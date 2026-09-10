package com.rmos.service;

import com.rmos.domain.DataSourceType;
import com.rmos.domain.IngestionStatus;
import com.rmos.domain.SourceDataRecord;
import com.rmos.dto.SourceDataRecordResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.SourceDataRecordRepository;
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
class SourceDataRecordServiceTest {

    @Mock
    private SourceDataRecordRepository repository;

    @InjectMocks
    private SourceDataRecordService service;

    private SourceDataRecord record;

    @BeforeEach
    void setUp() {
        record = new SourceDataRecord();
        record.setId(1L);
        record.setSourceRecordId("TMS-123");
        record.setSourceType(DataSourceType.TMS);
        record.setReceivedAt(LocalDateTime.now().minusHours(1));
        record.setLastUpdatedAt(LocalDateTime.now());
        record.setStatus(IngestionStatus.RECEIVED);
    }

    @Test
    void getAllSourceRecords_ReturnsList() {
        when(repository.findAll()).thenReturn(List.of(record));

        List<SourceDataRecordResponse> results = service.getAllSourceRecords();

        assertEquals(1, results.size());
        assertEquals("TMS-123", results.get(0).getSourceRecordId());
    }

    @Test
    void getSourceRecordById_ExistingId_ReturnsDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(record));

        SourceDataRecordResponse result = service.getSourceRecordById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(DataSourceType.TMS, result.getSourceType());
    }

    @Test
    void getSourceRecordById_NonExistingId_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getSourceRecordById(99L));
    }
}
