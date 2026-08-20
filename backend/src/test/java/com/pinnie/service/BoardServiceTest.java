package com.pinnie.service;

import com.pinnie.dto.BoardCreateRequestDTO;
import com.pinnie.dto.BoardResponseDTO;
import com.pinnie.dto.BoardUpdateRequestDTO;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Board;
import com.pinnie.model.User;
import com.pinnie.repository.BoardRepository;
import com.pinnie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardService boardService;

    private User owner;
    private User otherUser;
    private Board publicBoard;
    private Board privateBoard;
    private UUID ownerId;
    private UUID otherUserId;
    private UUID boardId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        boardId = UUID.randomUUID();

        owner = new User("owner", "owner@test.com", "hash", "Owner");
        org.springframework.test.util.ReflectionTestUtils.setField(owner, "id", ownerId);

        otherUser = new User("other", "other@test.com", "hash", "Other");
        org.springframework.test.util.ReflectionTestUtils.setField(otherUser, "id", otherUserId);

        publicBoard = new Board("Public Board", "Desc", false, owner);
        org.springframework.test.util.ReflectionTestUtils.setField(publicBoard, "id", boardId);

        privateBoard = new Board("Private Board", "Desc", true, owner);
        org.springframework.test.util.ReflectionTestUtils.setField(privateBoard, "id", boardId);
    }

    @Test
    void createBoard_Success() {
        BoardCreateRequestDTO request = new BoardCreateRequestDTO("New Board", "Desc", false);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
            Board b = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(b, "id", UUID.randomUUID());
            return b;
        });

        BoardResponseDTO response = boardService.createBoard(ownerId, request);

        assertNotNull(response.getId());
        assertEquals("New Board", response.getName());
        assertFalse(response.isPrivate());
        assertEquals(ownerId, response.getUserId());
    }

    @Test
    void getBoardById_PublicBoard_Success() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));

        BoardResponseDTO response = boardService.getBoardById(boardId, otherUserId);

        assertEquals("Public Board", response.getName());
    }

    @Test
    void getBoardById_PrivateBoard_Owner_Success() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(privateBoard));

        BoardResponseDTO response = boardService.getBoardById(boardId, ownerId);

        assertEquals("Private Board", response.getName());
    }

    @Test
    void getBoardById_PrivateBoard_OtherUser_ThrowsException() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(privateBoard));

        assertThrows(ResourceNotFoundException.class, () -> boardService.getBoardById(boardId, otherUserId));
    }

    @Test
    void updateBoard_Owner_Success() {
        BoardUpdateRequestDTO request = new BoardUpdateRequestDTO("Updated", "New Desc", true);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(publicBoard);

        BoardResponseDTO response = boardService.updateBoard(boardId, ownerId, request);

        assertEquals("Updated", response.getName());
        assertTrue(response.isPrivate());
    }

    @Test
    void updateBoard_OtherUser_ThrowsException() {
        BoardUpdateRequestDTO request = new BoardUpdateRequestDTO("Updated", "New Desc", true);

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));

        assertThrows(ResourceNotFoundException.class, () -> boardService.updateBoard(boardId, otherUserId, request));
    }

    @Test
    void deleteBoard_Owner_Success() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));

        boardService.deleteBoard(boardId, ownerId);

        verify(boardRepository).delete(publicBoard);
    }

    @Test
    void deleteBoard_OtherUser_ThrowsException() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));

        assertThrows(ResourceNotFoundException.class, () -> boardService.deleteBoard(boardId, otherUserId));
    }

    @Test
    void getUserBoards_Owner_ReturnsAll() {
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(boardRepository.findByUserId(ownerId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(publicBoard, privateBoard)));

        Page<BoardResponseDTO> result = boardService.getUserBoards(ownerId, ownerId, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void getUserBoards_OtherUser_ReturnsOnlyPublic() {
        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(boardRepository.findByUserIdAndIsPrivateFalse(ownerId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(publicBoard)));

        Page<BoardResponseDTO> result = boardService.getUserBoards(ownerId, otherUserId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Public Board", result.getContent().get(0).getName());
    }
}
