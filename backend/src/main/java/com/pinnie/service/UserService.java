package com.pinnie.service;

import com.pinnie.dto.RegisterRequestDTO;
import com.pinnie.dto.UserResponseDTO;
import com.pinnie.model.User;
import com.pinnie.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DataIntegrityViolationException("E-mail already exists");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new DataIntegrityViolationException("Username already exists");
        }

        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.displayName()
        );

        User savedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(savedUser);
    }
}
