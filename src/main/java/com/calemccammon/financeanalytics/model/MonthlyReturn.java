package com.calemccammon.financeanalytics.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Monthly return for a single ticker")
public record MonthlyReturn(
        @Schema(description = "Ticker symbol", example = "AAPL") String ticker,
        @Schema(description = "First calendar day of the month", example = "2025-01-01") LocalDate month,
        @Schema(description = "Monthly return as a decimal (0.05 = 5%)", example = "0.0618") Double monthlyReturnPct,
        @Schema(description = "Number of trading days in the month", example = "21") Integer tradingDays
) {}
