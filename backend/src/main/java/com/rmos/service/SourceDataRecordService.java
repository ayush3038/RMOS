package com.rmos.service;

import com.rmos.domain.SourceDataRecord;
import com.rmos.dto.SourceDataRecordResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.SourceDataRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SourceDataRecordService {

    private final SourceDataRecordRepository repository;

    public SourceDataRecordService(SourceDataRecordRepository repository) {
        this.repository = repository;
    }

    public List<SourceDataRecordResponse> getAllSourceRecords() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public SourceDataRecordResponse getSourceRecordById(Long id) {
        SourceDataRecord record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SourceDataRecord not found with id: " + id));
        return mapToResponse(record);
    }

    private SourceDataRecordResponse mapToResponse(SourceDataRecord record) {
        SourceDataRecordResponse response = new SourceDataRecordResponse();
        response.setId(record.getId());
        response.setSourceRecordId(record.getSourceRecordId());
        response.setSourceType(record.getSourceType());
        response.setReceivedAt(record.getReceivedAt());
        response.setLastUpdatedAt(record.getLastUpdatedAt());
        response.setStatus(record.getStatus());
        response.setValidationMessage(record.getValidationMessage());
        return response;
    }
}
