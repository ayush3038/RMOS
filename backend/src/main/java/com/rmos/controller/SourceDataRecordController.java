package com.rmos.controller;

import com.rmos.dto.SourceDataRecordResponse;
import com.rmos.service.SourceDataRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/source-records")
public class SourceDataRecordController {

    private final SourceDataRecordService sourceDataRecordService;

    public SourceDataRecordController(SourceDataRecordService sourceDataRecordService) {
        this.sourceDataRecordService = sourceDataRecordService;
    }

    @GetMapping
    public ResponseEntity<List<SourceDataRecordResponse>> getAllSourceRecords() {
        return ResponseEntity.ok(sourceDataRecordService.getAllSourceRecords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SourceDataRecordResponse> getSourceRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(sourceDataRecordService.getSourceRecordById(id));
    }
}
