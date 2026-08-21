package com.pinnie.service;

import com.pinnie.dto.PinResponseDTO;
import com.pinnie.model.Pin;
import com.pinnie.model.User;
import com.pinnie.repository.PinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PinRepository pinRepository;

    @InjectMocks
    private SearchService searchService;

    private User owner;
    private Pin pin1;
    private Pin pin2;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@test.com", "hash", "Owner");
        pin1 = new Pin(owner, "Receita de Bolo", "Bolo delicioso", "url1", "alt1", "file1.jpg", "image/jpeg", 1L, 1, 1);
        pin2 = new Pin(owner, "Viagem", "Dicas de viagem", "url2", "alt2", "file2.jpg", "image/jpeg", 2L, 2, 2);
    }

    @Test
    void searchPins_WithResults_ReturnsMappedPinsAndHasNext() {
        Slice<Pin> mockSlice = new SliceImpl<>(List.of(pin1), PageRequest.of(0, 1), true);
        when(pinRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                eq("Bolo"), eq("Bolo"), any())).thenReturn(mockSlice);

        Slice<PinResponseDTO> result = searchService.searchPins("Bolo", PageRequest.of(0, 1));

        assertEquals(1, result.getContent().size());
        assertTrue(result.hasNext());
        assertEquals("Receita de Bolo", result.getContent().get(0).getTitle());
        assertEquals("/uploads/file1.jpg", result.getContent().get(0).getImageUrl());
    }

    @Test
    void searchPins_EmptyResults_ReturnsEmptySlice() {
        Slice<Pin> mockSlice = new SliceImpl<>(List.of(), PageRequest.of(0, 2), false);
        when(pinRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                eq("Nada"), eq("Nada"), any())).thenReturn(mockSlice);

        Slice<PinResponseDTO> result = searchService.searchPins("Nada", PageRequest.of(0, 2));

        assertTrue(result.getContent().isEmpty());
        assertFalse(result.hasNext());
    }
}
