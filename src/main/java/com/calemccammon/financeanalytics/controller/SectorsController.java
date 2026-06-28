package com.calemccammon.financeanalytics.controller;

import com.calemccammon.financeanalytics.model.SectorSummary;
import com.calemccammon.financeanalytics.service.SectorsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/sectors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sectors", description = "Sector-level performance aggregations")
public class SectorsController {

    private final SectorsService service;

    public SectorsController(SectorsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @Operation(
        summary = "Sector performance summary",
        description = "Returns aggregated performance metrics for the latest trading day, " +
                      "grouped by GICS sector. Includes average return, volatility, " +
                      "volume, and percentage of stocks trading above their 200-day SMA."
    )
    @ApiResponse(responseCode = "200", description = "Sector summary for all sectors")
    public List<SectorSummary> summary() {
        return service.getSummary();
    }
}
