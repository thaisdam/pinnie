package com.pinnie.dto;

import com.pinnie.model.BoardPin;
import java.time.Instant;
import java.util.UUID;

public class BoardPinResponseDTO {
    
    private UUID id;
    private Instant savedAt;
    private PinResponseDTO pin;

    public BoardPinResponseDTO() {}

    public BoardPinResponseDTO(BoardPin boardPin, PinResponseDTO pinResponseDTO) {
        this.id = boardPin.getId();
        this.savedAt = boardPin.getCreatedAt();
        this.pin = pinResponseDTO;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(Instant savedAt) {
        this.savedAt = savedAt;
    }

    public PinResponseDTO getPin() {
        return pin;
    }

    public void setPin(PinResponseDTO pin) {
        this.pin = pin;
    }
}
