package com.calemccammon.financeanalytics.controller;

import com.calemccammon.financeanalytics.model.MonthlyReturn;
import com.calemccammon.financeanalytics.model.Performance52w;
import com.calemccammon.financeanalytics.model.StockSnapshot;
import com.calemccammon.financeanalytics.service.StocksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/stocks", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Stocks", description = "Stock price data and performance metrics")
public class StocksController {

    private final StocksService service;

    public StocksController(StocksService service) {
        this.service = service;
    }

    @GetMapping("/snapshot")
    @Operation(
        summary = "Latest trading-day snapshot",
        description = "Returns the most recent trading-day data for all tracked tickers, " +
                      "including price, daily return, moving averages, and volatility."
    )
    @ApiResponse(responseCode = "200", description = "Snapshot data for all tickers")
    public List<StockSnapshot> snapshot() {
        return service.getSnapshot();
    }

    @GetMapping("/performance")
    @Operation(
        summary = "52-week performance",
        description = "Returns 52-week return, average volatility, and average volume " +
                      "for all tracked tickers, sorted from best to worst performer."
    )
    @ApiResponse(responseCode = "200", description = "52-week performance for all tickers")
    public List<Performance52w> performance() {
        return service.getPerformance();
    }

    @GetMapping("/{ticker}/monthly-returns")
    @Operation(
        summary = "Monthly returns for a ticker",
        description = "Returns month-by-month return percentages for the specified ticker, " +
                      "most recent month first."
    )
    @ApiResponse(responseCode = "200", description = "Monthly returns")
    @ApiResponse(responseCode = "404", description = "Ticker not found or no data available")
    public List<MonthlyReturn> monthlyReturns(
            @PathVariable @Parameter(description = "Ticker symbol", example = "AAPL") String ticker) {
        return service.getMonthlyReturns(ticker.toUpperCase());
    }
}
