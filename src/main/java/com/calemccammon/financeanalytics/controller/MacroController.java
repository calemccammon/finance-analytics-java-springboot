package com.calemccammon.financeanalytics.controller;

import com.calemccammon.financeanalytics.model.MacroIndicator;
import com.calemccammon.financeanalytics.model.MacroVsStocks;
import com.calemccammon.financeanalytics.service.MacroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/macro", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Macro", description = "Federal Reserve macro indicators (FRED data)")
public class MacroController {

    private final MacroService service;

    public MacroController(MacroService service) {
        this.service = service;
    }

    @GetMapping("/indicators")
    @Operation(
        summary = "Macro indicator history",
        description = "Returns daily macro indicator values (Fed Funds Rate, unemployment, " +
                      "CPI, treasury yields) forward-filled to all trading dates."
    )
    @ApiResponse(responseCode = "200", description = "Macro indicator time series")
    public List<MacroIndicator> indicators() {
        return service.getIndicators();
    }

    @GetMapping("/vs-stocks")
    @Operation(
        summary = "Stock returns by rate regime",
        description = "Returns average daily return and volatility statistics grouped by " +
                      "Federal Funds Rate regime (High ≥5%, Elevated 3–5%, Normal 1–3%, Low <1%)."
    )
    @ApiResponse(responseCode = "200", description = "Return statistics by rate regime")
    public List<MacroVsStocks> vsStocks() {
        return service.getVsStocks();
    }
}
