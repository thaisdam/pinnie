package com.pinnie.service;

import com.pinnie.dto.PinCreateRequestDTO;
import com.pinnie.dto.PinResponseDTO;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.PendingImageUpload;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.PendingImageUploadRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PinServiceTest {

    private PinRepository pinRepository = mock(PinRepository.class);
    private PendingImageUploadRepository pendingImageUploadRepository = mock(PendingImageUploadRepository.class);
    private UserRepository userRepository = mock(UserRepository.class);
    private ImageStorageService imageStorageService = mock(ImageStorageService.class);
    private ImageProcessor imageProcessor = new ImageProcessor();

    private PinService pinService;

    private User owner;
    private User otherUser;
    private UUID ownerId;
    private UUID otherUserId;
    private UUID uploadId;

    @BeforeEach
    void setUp() {
        pinService = new PinService(pinRepository, pendingImageUploadRepository, userRepository, imageStorageService, imageProcessor);

        ownerId = UUID.randomUUID();
        owner = new User("owner", "owner@test.com", "hash", "Owner");
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(owner, ownerId);
        } catch (Exception ignored) {}

        otherUserId = UUID.randomUUID();
        otherUser = new User("other", "other@test.com", "hash", "Other");
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(otherUser, otherUserId);
        } catch (Exception ignored) {}

        uploadId = UUID.randomUUID();
    }

    @Test
    void createPin_WithValidUploadId_CreatesPin() {
        PendingImageUpload pending = new PendingImageUpload(uploadId, owner, "file.jpg", "image/jpeg", 1000L, 800, 600, Instant.now().plusSeconds(3600));

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(pendingImageUploadRepository.findById(uploadId)).thenReturn(Optional.of(pending));
        when(pinRepository.save(any(Pin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PinCreateRequestDTO request = new PinCreateRequestDTO("Title", "Desc", "http://example.com", "Alt", uploadId);

        PinResponseDTO response = pinService.createPin(request, ownerId);

        assertNotNull(response);
        assertEquals("Title", response.getTitle());
        assertEquals("/uploads/file.jpg", response.getImageUrl());
        verify(pendingImageUploadRepository, times(1)).delete(pending);
    }

    @Test
    void createPin_WithOtherUserUploadId_ThrowsException() {
        PendingImageUpload pending = new PendingImageUpload(uploadId, otherUser, "file.jpg", "image/jpeg", 1000L, 800, 600, Instant.now().plusSeconds(3600));

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(pendingImageUploadRepository.findById(uploadId)).thenReturn(Optional.of(pending));

        PinCreateRequestDTO request = new PinCreateRequestDTO("Title", "Desc", "http://example.com", "Alt", uploadId);

        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(request, ownerId));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void deletePin_ByOwner_DeletesPinAndImage() {
        Pin pin = new Pin(owner, "Title", "Desc", "http://test", "alt", "file.jpg", "image/jpeg", 100L, 100, 100);

        when(pinRepository.findById(any())).thenReturn(Optional.of(pin));

        pinService.deletePin(UUID.randomUUID(), ownerId);

        verify(pinRepository, times(1)).delete(pin);
        verify(imageStorageService, times(1)).delete("file.jpg");
    }

    @Test
    void deletePin_ByOtherUser_ThrowsNotFound() {
        Pin pin = new Pin(owner, "Title", "Desc", "http://test", "alt", "file.jpg", "image/jpeg", 100L, 100, 100);

        when(pinRepository.findById(any())).thenReturn(Optional.of(pin));

        assertThrows(ResourceNotFoundException.class, () -> pinService.deletePin(UUID.randomUUID(), otherUserId));
        verify(pinRepository, never()).delete(any());
    }
}
