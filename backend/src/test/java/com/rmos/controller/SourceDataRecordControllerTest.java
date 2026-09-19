package com.rmos.controller;

import com.rmos.domain.DataSourceType;
import com.rmos.domain.IngestionStatus;
import com.rmos.dto.SourceDataRecordResponse;
import com.rmos.service.SourceDataRecordService;
import com.rmos.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import({com.rmos.security.SecurityConfig.class, com.rmos.security.JwtAuthFilter.class, com.rmos.security.CustomAuthenticationEntryPoint.class, com.rmos.security.CustomAccessDeniedHandler.class})
@WebMvcTest(SourceDataRecordController.class)
@org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
class SourceDataRecordControllerTest {
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.rmos.security.JwtUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SourceDataRecordService sourceDataRecordService;

    @Test
    void getAllSourceRecords_Returns200() throws Exception {
        SourceDataRecordResponse response = new SourceDataRecordResponse();
        response.setId(1L);
        response.setSourceRecordId("TMS-1");
        response.setSourceType(DataSourceType.TMS);
        response.setStatus(IngestionStatus.RECEIVED);

        when(sourceDataRecordService.getAllSourceRecords()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/integration/source-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sourceRecordId").value("TMS-1"))
                .andExpect(jsonPath("$[0].sourceType").value("TMS"))
                .andExpect(jsonPath("$[0].status").value("RECEIVED"));
    }

    @Test
    void getSourceRecordById_ExistingId_Returns200() throws Exception {
        SourceDataRecordResponse response = new SourceDataRecordResponse();
        response.setId(1L);
        response.setSourceRecordId("TMS-1");

        when(sourceDataRecordService.getSourceRecordById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/integration/source-records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sourceRecordId").value("TMS-1"));
    }

    @Test
    void getSourceRecordById_NonExistingId_Returns404() throws Exception {
        when(sourceDataRecordService.getSourceRecordById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/integration/source-records/99"))
                .andExpect(status().isNotFound());
    }
}





