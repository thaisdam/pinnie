package com.pinnie.service;

import com.pinnie.dto.BoardCreateRequestDTO;
import com.pinnie.dto.BoardResponseDTO;
import com.pinnie.dto.BoardUpdateRequestDTO;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.Board;
import com.pinnie.model.User;
import com.pinnie.repository.BoardRepository;
import com.pinnie.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    public BoardResponseDTO createBoard(UUID requesterId, BoardCreateRequestDTO request) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = new Board(request.getName(), request.getDescription(), request.isPrivate(), user);
        board = boardRepository.save(board);

        return new BoardResponseDTO(board);
    }

    public BoardResponseDTO getBoardById(UUID boardId, UUID requesterId) {
        Board board = findBoardOrThrow404(boardId);

        if (board.isPrivate() && !board.getUser().getId().equals(requesterId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        return new BoardResponseDTO(board);
    }

    public BoardResponseDTO updateBoard(UUID boardId, UUID requesterId, BoardUpdateRequestDTO request) {
        Board board = findBoardOrThrow404(boardId);

        if (!board.getUser().getId().equals(requesterId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setPrivate(request.isPrivate());

        board = boardRepository.save(board);

        return new BoardResponseDTO(board);
    }

    public void deleteBoard(UUID boardId, UUID requesterId) {
        Board board = findBoardOrThrow404(boardId);

        if (!board.getUser().getId().equals(requesterId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        boardRepository.delete(board);
    }

    public Page<BoardResponseDTO> getUserBoards(UUID targetUserId, UUID requesterId, Pageable pageable) {
        if (!userRepository.existsById(targetUserId)) {
            throw new ResourceNotFoundException("User not found");
        }

        Page<Board> boards;
        if (targetUserId.equals(requesterId)) {
            boards = boardRepository.findByUserId(targetUserId, pageable);
        } else {
            boards = boardRepository.findByUserIdAndIsPrivateFalse(targetUserId, pageable);
        }

        return boards.map(BoardResponseDTO::new);
    }

    private Board findBoardOrThrow404(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
    }
}
