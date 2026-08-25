package com.pinnie.dto;

import com.pinnie.model.BoardPin;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representa a associação de um Pin salvo dentro de uma Pasta")
public class BoardPinResponseDTO {
    
    @Schema(description = "ID do registro de salvamento na pasta")
    private UUID id;
    @Schema(description = "Data em que o Pin foi salvo na pasta")
    private Instant savedAt;
    @Schema(description = "Dados detalhados do Pin salvo")
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
