package com.calemccammon.financeanalytics.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Daily macro indicator values (FRED data, forward-filled to trading dates)")
public record MacroIndicator(
        @Schema(description = "Trading date", example = "2025-06-27") LocalDate tradingDate,
        @Schema(description = "Federal Funds Rate as a decimal (0.05 = 5%)", example = "0.0363") Double fedFundsRate,
        @Schema(description = "CPI index value (not a percentage)", example = "314.540") Double cpi,
        @Schema(description = "Unemployment rate as a decimal (0.04 = 4%)", example = "0.043") Double unemploymentRate,
        @Schema(description = "10-year Treasury yield as a decimal", example = "0.0449") Double treasuryYield10y,
        @Schema(description = "GDP growth rate as a decimal", example = "0.025") Double gdpGrowthRate,
        @Schema(description = "10-year breakeven inflation as a decimal", example = "0.0225") Double breakevenInflation10y,
        @Schema(description = "10-year real yield as a decimal", example = "0.0224") Double realYield10y
) {}
