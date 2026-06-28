package com.calemccammon.financeanalytics.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sector-level aggregated performance for the latest trading day")
public record SectorSummary(
        @Schema(description = "GICS sector", example = "Technology") String sector,
        @Schema(description = "Number of tracked stocks in this sector", example = "4") Integer stockCount,
        @Schema(description = "Average daily return as a decimal", example = "0.00185") Double avgDailyReturnPct,
        @Schema(description = "Average 20-day annualised volatility as a decimal", example = "0.2841") Double avgVolatility20dPct,
        @Schema(description = "Average trading volume across sector stocks", example = "45123456.0") Double avgVolume,
        @Schema(description = "Average % above 200-day SMA as a decimal (0.10 = 10% above)", example = "0.0823") Double avgPctAbove200sma
) {}
