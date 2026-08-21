package com.pinnie.service;

import com.pinnie.dto.BoardPinResponseDTO;
import com.pinnie.dto.PinResponseDTO;
import com.pinnie.exception.DuplicateResourceException;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Board;
import com.pinnie.model.BoardPin;
import com.pinnie.model.Pin;
import com.pinnie.repository.BoardPinRepository;
import com.pinnie.repository.BoardRepository;
import com.pinnie.repository.PinRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BoardPinService {

    private final BoardPinRepository boardPinRepository;
    private final BoardRepository boardRepository;
    private final PinRepository pinRepository;

    public BoardPinService(BoardPinRepository boardPinRepository,
                           BoardRepository boardRepository,
                           PinRepository pinRepository) {
        this.boardPinRepository = boardPinRepository;
        this.boardRepository = boardRepository;
        this.pinRepository = pinRepository;
    }

    @Transactional
    public void savePinToBoard(UUID boardId, UUID pinId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Board not found"); // Opacity for other's boards
        }

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));

        if (boardPinRepository.existsByBoardIdAndPinId(boardId, pinId)) {
            throw new DuplicateResourceException("This Pin is already saved to this Board");
        }

        BoardPin boardPin = new BoardPin(board, pin);
        boardPinRepository.save(boardPin);
    }

    @Transactional
    public void removePinFromBoard(UUID boardId, UUID pinId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Board not found"); // Opacity for other's boards
        }

        BoardPin boardPin = boardPinRepository.findByBoardIdAndPinId(boardId, pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found in this Board"));

        boardPinRepository.delete(boardPin);
    }

    @Transactional(readOnly = true)
    public Page<BoardPinResponseDTO> getPinsFromBoard(UUID boardId, UUID userId, Pageable pageable) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (board.isPrivate() && !board.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        return boardPinRepository.findByBoardId(boardId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public BoardPinResponseDTO checkPinInBoard(UUID boardId, UUID pinId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (board.isPrivate() && !board.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        BoardPin boardPin = boardPinRepository.findByBoardIdAndPinId(boardId, pinId)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found in this Board"));

        return mapToResponseDTO(boardPin);
    }

    private BoardPinResponseDTO mapToResponseDTO(BoardPin boardPin) {
        Pin pin = boardPin.getPin();
        String imageUrl = "/uploads/" + pin.getMediaStoredFilename();
        PinResponseDTO pinDTO = new PinResponseDTO(pin, imageUrl);
        return new BoardPinResponseDTO(boardPin, pinDTO);
    }
}
