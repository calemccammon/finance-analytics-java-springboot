package com.calemccammon.financeanalytics.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "52-week return performance for a single ticker")
public record Performance52w(
        @Schema(description = "Ticker symbol", example = "NVDA") String ticker,
        @Schema(description = "Company name", example = "NVIDIA Corporation") String companyName,
        @Schema(description = "GICS sector", example = "Technology") String sector,
        @Schema(description = "Closing price 52 weeks ago", example = "495.22") Double price52wAgo,
        @Schema(description = "Current closing price", example = "875.40") Double priceNow,
        @Schema(description = "52-week return as a decimal (0.15 = 15%)", example = "0.7677") Double return52wPct,
        @Schema(description = "Average 20-day annualised volatility as a decimal", example = "0.4521") Double avgVolatilityPct,
        @Schema(description = "Average daily trading volume", example = "41234567.0") Double avgDailyVolume
) {}
