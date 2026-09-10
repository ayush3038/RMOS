package com.rmos.service;

import com.rmos.domain.CanonicalAssetMapping;
import com.rmos.domain.DataSourceType;
import com.rmos.dto.CanonicalAssetMappingResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.CanonicalAssetMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CanonicalAssetMappingServiceTest {

    @Mock
    private CanonicalAssetMappingRepository repository;

    @InjectMocks
    private CanonicalAssetMappingService service;

    private CanonicalAssetMapping mapping;

    @BeforeEach
    void setUp() {
        mapping = new CanonicalAssetMapping();
        mapping.setId(1L);
        mapping.setSourceAssetId("SMMS-A1");
        mapping.setSourceType(DataSourceType.SMMS);
        mapping.setCanonicalAssetId("CANON-123");
        mapping.setActive(true);
        mapping.setMappedAt(LocalDateTime.now());
    }

    @Test
    void getAllMappings_ReturnsList() {
        when(repository.findAll()).thenReturn(List.of(mapping));

        List<CanonicalAssetMappingResponse> results = service.getAllMappings();

        assertEquals(1, results.size());
        assertEquals("CANON-123", results.get(0).getCanonicalAssetId());
    }

    @Test
    void getMappingById_ExistingId_ReturnsDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(mapping));

        CanonicalAssetMappingResponse result = service.getMappingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(DataSourceType.SMMS, result.getSourceType());
    }

    @Test
    void getMappingById_NonExistingId_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getMappingById(99L));
    }
}
