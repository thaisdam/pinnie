package com.pinnie.service;

import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Comment;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.CommentRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PinRepository pinRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User pinOwner;
    private User commentAuthor;
    private User thirdUser;
    private Pin pin;
    private Comment comment;
    
    @BeforeEach
    void setUp() {
        pinOwner = new User();
        ReflectionTestUtils.setField(pinOwner, "id", UUID.randomUUID());

        commentAuthor = new User();
        ReflectionTestUtils.setField(commentAuthor, "id", UUID.randomUUID());

        thirdUser = new User();
        ReflectionTestUtils.setField(thirdUser, "id", UUID.randomUUID());

        pin = new Pin();
        ReflectionTestUtils.setField(pin, "id", UUID.randomUUID());
        pin.setUser(pinOwner);

        comment = new Comment(pin, commentAuthor, "Nice pin!");
        ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
    }

    @Test
    void createComment_ValidPin_Success() { // 1. usuário cria comentário em Pin existente
        when(pinRepository.findById(pin.getId())).thenReturn(Optional.of(pin));
        when(userRepository.findById(commentAuthor.getId())).thenReturn(Optional.of(commentAuthor));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

        Comment created = commentService.createComment(pin.getId(), commentAuthor.getId(), "Great!");

        assertNotNull(created);
        assertEquals("Great!", created.getText());
        assertEquals(commentAuthor, created.getUser());
        assertEquals(pin, created.getPin());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_UnauthenticatedUser_ThrowsException() { // 2. usuário não autenticado não consegue criar
        assertThrows(AccessDeniedException.class, () -> 
            commentService.createComment(pin.getId(), null, "Great!")
        );
    }

    @Test
    void deleteComment_AsCommentAuthor_Success() { // 3. autor consegue excluir seu comentário
        when(pinRepository.findById(pin.getId())).thenReturn(Optional.of(pin));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(pin.getId(), comment.getId(), commentAuthor.getId());

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_AsPinOwner_Success() { // 4. proprietário do Pin consegue excluir comentário de outro usuário
        when(pinRepository.findById(pin.getId())).thenReturn(Optional.of(pin));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(pin.getId(), comment.getId(), pinOwner.getId());

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_AsThirdUser_ThrowsAccessDeniedException() { // 5. terceiro usuário NÃO consegue excluir
        when(pinRepository.findById(pin.getId())).thenReturn(Optional.of(pin));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> 
            commentService.deleteComment(pin.getId(), comment.getId(), thirdUser.getId())
        );
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_CommentBelongsToAnotherPin_ThrowsResourceNotFoundException() { // 6. commentId pertencente a outro Pin não pode ser excluído pela rota
        Pin anotherPin = new Pin();
        ReflectionTestUtils.setField(anotherPin, "id", UUID.randomUUID());
        
        when(pinRepository.findById(anotherPin.getId())).thenReturn(Optional.of(anotherPin));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        assertThrows(ResourceNotFoundException.class, () -> 
            commentService.deleteComment(anotherPin.getId(), comment.getId(), commentAuthor.getId())
        );
    }

    @Test
    void createComment_PinNotFound_ThrowsException() { // 7. Pin inexistente
        when(pinRepository.findById(pin.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            commentService.createComment(pin.getId(), commentAuthor.getId(), "Great!")
        );
    }

    @Test
    void deleteComment_CommentNotFound_ThrowsException() { // 8. comentário inexistente
        when(pinRepository.findById(pin.getId())).thenReturn(Optional.of(pin));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            commentService.deleteComment(pin.getId(), comment.getId(), commentAuthor.getId())
        );
    }

    @Test
    void createComment_BlankText_ThrowsException() { // 9. texto vazio
        assertThrows(IllegalArgumentException.class, () -> 
            commentService.createComment(pin.getId(), commentAuthor.getId(), "   ")
        );
    }

    @Test
    void createComment_TextTooLong_ThrowsException() { // 10. texto acima de 500 caracteres
        String longText = "a".repeat(501);
        assertThrows(IllegalArgumentException.class, () -> 
            commentService.createComment(pin.getId(), commentAuthor.getId(), longText)
        );
    }
}
