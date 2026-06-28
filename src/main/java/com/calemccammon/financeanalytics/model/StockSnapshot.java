package com.calemccammon.financeanalytics.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Latest trading-day snapshot for a single ticker")
public record StockSnapshot(
        @Schema(description = "Ticker symbol", example = "AAPL") String ticker,
        @Schema(description = "Company name", example = "Apple Inc.") String companyName,
        @Schema(description = "GICS sector", example = "Technology") String sector,
        @Schema(description = "GICS industry", example = "Consumer Electronics") String industry,
        @Schema(description = "Market cap bucket", example = "Mega Cap (>$1T)") String marketCapCategory,
        @Schema(description = "Exchange", example = "NMS") String exchange,
        @Schema(description = "Closing price in USD", example = "211.45") Double latestClose,
        @Schema(description = "Daily return as a decimal (0.002 = 0.2%)", example = "0.0018") Double dailyReturnPct,
        @Schema(description = "Trading volume", example = "58234100") Long volume,
        @Schema(description = "30-day simple moving average", example = "206.30") Double sma30d,
        @Schema(description = "200-day simple moving average", example = "198.75") Double sma200d,
        @Schema(description = "20-day annualised volatility as a decimal", example = "0.2341") Double volatility20dPct,
        @Schema(description = "Date of this snapshot", example = "2025-06-27") LocalDate asOfDate
) {}
