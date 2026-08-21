package com.pinnie.service;

import com.pinnie.dto.PinResponseDTO;
import com.pinnie.model.Pin;
import com.pinnie.repository.PinRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final PinRepository pinRepository;

    public FeedService(PinRepository pinRepository) {
        this.pinRepository = pinRepository;
    }

    @Transactional(readOnly = true)
    public Slice<PinResponseDTO> getFeed(Pageable pageable) {
        Slice<Pin> pins = pinRepository.findAllByOrderByCreatedAtDesc(pageable);
        return pins.map(this::mapToResponseDTO);
    }

    private PinResponseDTO mapToResponseDTO(Pin pin) {
        String imageUrl = "/uploads/" + pin.getMediaStoredFilename();
        return new PinResponseDTO(pin, imageUrl);
    }
}
