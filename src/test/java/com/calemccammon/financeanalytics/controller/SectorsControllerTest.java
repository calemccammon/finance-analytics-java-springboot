package com.calemccammon.financeanalytics.controller;

import com.calemccammon.financeanalytics.model.SectorSummary;
import com.calemccammon.financeanalytics.service.SectorsService;
import com.google.cloud.bigquery.BigQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SectorsController.class)
class SectorsControllerTest {

    @Autowired MockMvc mvc;
    @MockBean SectorsService service;
    @MockBean BigQuery bigQuery;

    @Test
    void summary_returns200WithJsonArray() throws Exception {
        when(service.getSummary()).thenReturn(List.of(
                new SectorSummary("Technology", 4, 0.00185, 0.2841, 45123456.0, 0.0823)));

        mvc.perform(get("/api/sectors/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sector").value("Technology"))
                .andExpect(jsonPath("$[0].stockCount").value(4))
                .andExpect(jsonPath("$[0].avgDailyReturnPct").value(0.00185));
    }

    @Test
    void summary_returnsEmptyArray_whenNoData() throws Exception {
        when(service.getSummary()).thenReturn(List.of());

        mvc.perform(get("/api/sectors/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
