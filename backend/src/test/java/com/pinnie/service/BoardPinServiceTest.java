package com.pinnie.service;

import com.pinnie.dto.BoardPinResponseDTO;
import com.pinnie.exception.DuplicateResourceException;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Board;
import com.pinnie.model.BoardPin;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.BoardPinRepository;
import com.pinnie.repository.BoardRepository;
import com.pinnie.repository.PinRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardPinServiceTest {

    @Mock
    private BoardPinRepository boardPinRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PinRepository pinRepository;

    @InjectMocks
    private BoardPinService boardPinService;

    private User owner;
    private User other;
    private UUID ownerId;
    private UUID otherId;
    private Board publicBoard;
    private Board privateBoard;
    private Pin pin;
    private UUID boardId;
    private UUID pinId;

    @BeforeEach
    void setUp() throws Exception {
        ownerId = UUID.randomUUID();
        owner = new User("owner", "owner@test.com", "hash", "Owner");
        var fieldId = User.class.getDeclaredField("id");
        fieldId.setAccessible(true);
        fieldId.set(owner, ownerId);

        otherId = UUID.randomUUID();
        other = new User("other", "other@test.com", "hash", "Other");
        fieldId.set(other, otherId);

        boardId = UUID.randomUUID();
        publicBoard = new Board("Public", "Desc", false, owner);
        privateBoard = new Board("Private", "Desc", true, owner);
        
        pinId = UUID.randomUUID();
        pin = new Pin(other, "Pin", "Desc", "url", "alt", "img.jpg", "image/jpeg", 1L, 1, 1);
    }

    @Test
    void savePinToBoard_ByOwner_Success() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));
        when(pinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(boardPinRepository.existsByBoardIdAndPinId(boardId, pinId)).thenReturn(false);

        assertDoesNotThrow(() -> boardPinService.savePinToBoard(boardId, pinId, ownerId));
        verify(boardPinRepository, times(1)).save(any(BoardPin.class));
    }

    @Test
    void savePinToBoard_ByOtherUser_ThrowsNotFound() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));

        assertThrows(ResourceNotFoundException.class, () -> boardPinService.savePinToBoard(boardId, pinId, otherId));
        verify(boardPinRepository, never()).save(any());
    }

    @Test
    void savePinToBoard_Duplicate_ThrowsConflict() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));
        when(pinRepository.findById(pinId)).thenReturn(Optional.of(pin));
        when(boardPinRepository.existsByBoardIdAndPinId(boardId, pinId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> boardPinService.savePinToBoard(boardId, pinId, ownerId));
    }

    @Test
    void removePinFromBoard_ByOwner_Success() {
        BoardPin boardPin = new BoardPin(publicBoard, pin);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));
        when(boardPinRepository.findByBoardIdAndPinId(boardId, pinId)).thenReturn(Optional.of(boardPin));

        assertDoesNotThrow(() -> boardPinService.removePinFromBoard(boardId, pinId, ownerId));
        verify(boardPinRepository, times(1)).delete(boardPin);
    }

    @Test
    void getPinsFromBoard_PublicBoard_SuccessForAnyone() {
        BoardPin boardPin = new BoardPin(publicBoard, pin);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(publicBoard));
        when(boardPinRepository.findByBoardId(eq(boardId), any())).thenReturn(new PageImpl<>(List.of(boardPin)));

        Page<BoardPinResponseDTO> page = boardPinService.getPinsFromBoard(boardId, otherId, PageRequest.of(0, 10));
        assertFalse(page.isEmpty());
    }

    @Test
    void getPinsFromBoard_PrivateBoard_ThrowsNotFoundForOtherUser() {
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(privateBoard));

        assertThrows(ResourceNotFoundException.class, () -> boardPinService.getPinsFromBoard(boardId, otherId, PageRequest.of(0, 10)));
    }
}
