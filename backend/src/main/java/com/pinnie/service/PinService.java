package com.pinnie.service;

import com.pinnie.dto.ImageUploadResponseDTO;
import com.pinnie.dto.PinCreateRequestDTO;
import com.pinnie.dto.PinResponseDTO;
import com.pinnie.dto.PinUpdateRequestDTO;
import com.pinnie.exception.ResourceNotFoundException;
import com.pinnie.model.PendingImageUpload;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.PendingImageUploadRepository;
import com.pinnie.repository.PinRepository;
import com.pinnie.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PinService {

    private final PinRepository pinRepository;
    private final PendingImageUploadRepository pendingImageUploadRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final ImageProcessor imageProcessor;

    public PinService(PinRepository pinRepository,
                      PendingImageUploadRepository pendingImageUploadRepository,
                      UserRepository userRepository,
                      ImageStorageService imageStorageService,
                      ImageProcessor imageProcessor) {
        this.pinRepository = pinRepository;
        this.pendingImageUploadRepository = pendingImageUploadRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
        this.imageProcessor = imageProcessor;
    }

    @Transactional
    public ImageUploadResponseDTO processAndStoreImage(byte[] imageBytes, UUID userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ImageProcessor.ImageInfo info = imageProcessor.processAndValidate(imageBytes);

        String extension = "";
        if (info.getMimeType().equals("image/jpeg")) extension = ".jpg";
        else if (info.getMimeType().equals("image/png")) extension = ".png";
        else if (info.getMimeType().equals("image/webp")) extension = ".webp";

        String storedFilename = imageStorageService.store(imageBytes, extension);

        PendingImageUpload pending = new PendingImageUpload(
                UUID.randomUUID(),
                user,
                storedFilename,
                info.getMimeType(),
                imageBytes.length,
                info.getWidth(),
                info.getHeight(),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );

        pendingImageUploadRepository.save(pending);

        return new ImageUploadResponseDTO(pending.getId(), info.getWidth(), info.getHeight());
    }

    @Transactional
    public PinResponseDTO createPin(PinCreateRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PendingImageUpload pending = pendingImageUploadRepository.findById(request.getUploadId())
                .orElseThrow(() -> new IllegalArgumentException("Upload ID not found or already consumed"));

        if (!pending.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Upload ID does not belong to the current user");
        }

        Pin pin = new Pin(
                user,
                request.getTitle(),
                request.getDescription(),
                request.getLink(),
                request.getAltText(),
                pending.getStoredFilename(),
                pending.getContentType(),
                pending.getSize(),
                pending.getWidth(),
                pending.getHeight()
        );

        pin = pinRepository.save(pin);
        pendingImageUploadRepository.delete(pending);

        return mapToResponseDTO(pin);
    }

    @Transactional(readOnly = true)
    public PinResponseDTO getPin(UUID id) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));
        return mapToResponseDTO(pin);
    }

    @Transactional(readOnly = true)
    public Page<PinResponseDTO> getUserPins(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        return pinRepository.findByUserId(userId, pageable).map(this::mapToResponseDTO);
    }

    @Transactional
    public PinResponseDTO updatePin(UUID id, PinUpdateRequestDTO request, UUID userId) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));

        if (!pin.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Pin not found"); // returns 404 instead of 403
        }

        pin.setTitle(request.getTitle());
        pin.setDescription(request.getDescription());
        pin.setLink(request.getLink());
        pin.setAltText(request.getAltText());

        pin = pinRepository.save(pin);
        return mapToResponseDTO(pin);
    }

    @Transactional
    public void deletePin(UUID id, UUID userId) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));

        if (!pin.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Pin not found"); // returns 404 instead of 403
        }

        String storedFilename = pin.getMediaStoredFilename();
        pinRepository.delete(pin);

        // Transaction successful for DB, now try deleting the file
        imageStorageService.delete(storedFilename);
    }

    @Transactional
    public void deletePinAsAdmin(UUID id) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pin not found"));

        String storedFilename = pin.getMediaStoredFilename();
        pinRepository.delete(pin);

        imageStorageService.delete(storedFilename);
    }

    private PinResponseDTO mapToResponseDTO(Pin pin) {
        String imageUrl = "/uploads/" + pin.getMediaStoredFilename();
        return new PinResponseDTO(pin, imageUrl);
    }
}
