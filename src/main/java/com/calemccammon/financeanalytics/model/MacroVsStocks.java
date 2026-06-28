package com.calemccammon.financeanalytics.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stock return statistics grouped by Federal Funds Rate regime")
public record MacroVsStocks(
        @Schema(description = "Rate regime label", example = "Elevated Rates (3-5%)") String rateRegime,
        @Schema(description = "Average daily return as a decimal", example = "0.001") Double avgDailyReturnPct,
        @Schema(description = "Standard deviation of daily returns as a decimal", example = "0.022") Double stdDevPct,
        @Schema(description = "Number of ticker-day observations in this regime", example = "4300") Long observations
) {}
