package com.calemccammon.financeanalytics.controller;

import com.calemccammon.financeanalytics.model.MacroIndicator;
import com.calemccammon.financeanalytics.model.MacroVsStocks;
import com.calemccammon.financeanalytics.service.MacroService;
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

@WebMvcTest(MacroController.class)
class MacroControllerTest {

    @Autowired MockMvc mvc;
    @MockBean MacroService service;
    @MockBean BigQuery bigQuery;

    @Test
    void indicators_returns200WithJsonArray() throws Exception {
        when(service.getIndicators()).thenReturn(List.of(
                new MacroIndicator(LocalDate.of(2025, 6, 27),
                        0.0363, 314.54, 0.043, 0.0449, 0.025, 0.0225, 0.0224)));

        mvc.perform(get("/api/macro/indicators").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].tradingDate").value("2025-06-27"))
                .andExpect(jsonPath("$[0].fedFundsRate").value(0.0363));
    }

    @Test
    void vsStocks_returns200WithJsonArray() throws Exception {
        when(service.getVsStocks()).thenReturn(List.of(
                new MacroVsStocks("Elevated Rates (3–5%)", 0.001, 0.022, 4300L)));

        mvc.perform(get("/api/macro/vs-stocks").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rateRegime").value("Elevated Rates (3–5%)"))
                .andExpect(jsonPath("$[0].observations").value(4300));
    }

    @Test
    void indicators_returnsEmptyArray_whenNoData() throws Exception {
        when(service.getIndicators()).thenReturn(List.of());

        mvc.perform(get("/api/macro/indicators").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
