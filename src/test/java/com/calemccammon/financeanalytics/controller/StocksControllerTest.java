package com.calemccammon.financeanalytics.controller;

import com.calemccammon.financeanalytics.model.MonthlyReturn;
import com.calemccammon.financeanalytics.model.Performance52w;
import com.calemccammon.financeanalytics.model.StockSnapshot;
import com.calemccammon.financeanalytics.service.StocksService;
import com.google.cloud.bigquery.BigQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StocksController.class)
class StocksControllerTest {

    @Autowired MockMvc mvc;
    @MockBean StocksService service;
    @MockBean BigQuery bigQuery; // prevents BigQueryConfig from requiring GCP credentials

    @Test
    void snapshot_returns200WithJsonArray() throws Exception {
        when(service.getSnapshot()).thenReturn(List.of(
                new StockSnapshot("AAPL", "Apple Inc.", "Technology", "Consumer Electronics",
                        "Mega Cap (>$1T)", "NMS", 211.45, 0.0018, 58234100L,
                        206.30, 198.75, 0.2341, LocalDate.of(2025, 6, 27))));

        mvc.perform(get("/api/stocks/snapshot").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].companyName").value("Apple Inc."))
                .andExpect(jsonPath("$[0].latestClose").value(211.45));
    }

    @Test
    void snapshot_returnsEmptyArray_whenNoData() throws Exception {
        when(service.getSnapshot()).thenReturn(List.of());

        mvc.perform(get("/api/stocks/snapshot").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void performance_returns200WithJsonArray() throws Exception {
        when(service.getPerformance()).thenReturn(List.of(
                new Performance52w("NVDA", "NVIDIA Corporation", "Technology",
                        495.22, 875.40, 0.7677, 0.4521, 41234567.0)));

        mvc.perform(get("/api/stocks/performance").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("NVDA"))
                .andExpect(jsonPath("$[0].return52wPct").value(0.7677));
    }

    @Test
    void monthlyReturns_returns200ForValidTicker() throws Exception {
        when(service.getMonthlyReturns("AAPL")).thenReturn(List.of(
                new MonthlyReturn("AAPL", LocalDate.of(2025, 1, 1), 0.0618, 21)));

        mvc.perform(get("/api/stocks/AAPL/monthly-returns").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].monthlyReturnPct").value(0.0618));
    }

    @Test
    void monthlyReturns_uppercasesTicker() throws Exception {
        when(service.getMonthlyReturns("AAPL")).thenReturn(List.of());

        mvc.perform(get("/api/stocks/aapl/monthly-returns").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
