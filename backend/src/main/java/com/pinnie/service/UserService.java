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
    private final ImageStorageService imageStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorageService = imageStorageService;
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

    public User updateProfile(java.util.UUID userId, com.pinnie.dto.UserProfileUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDisplayName(request.displayName());
        user.setBio(request.bio());
        return userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public void blockUser(java.util.UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.pinnie.exception.ResourceNotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void updatePassword(java.util.UUID userId, com.pinnie.dto.UserPasswordUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Senha antiga incorreta");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public User updateAvatar(java.util.UUID userId, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.lastIndexOf('.') > 0) {
                extension = originalName.substring(originalName.lastIndexOf('.') + 1);
            }
            String avatarFilename = imageStorageService.store(file.getBytes(), extension);
            // Salvar no banco como uma rota relativa
            user.setAvatarUrl("/uploads/" + avatarFilename);
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store avatar", e);
        }
    }
}
