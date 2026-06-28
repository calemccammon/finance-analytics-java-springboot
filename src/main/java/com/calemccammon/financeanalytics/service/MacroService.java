package com.calemccammon.financeanalytics.service;

import com.calemccammon.financeanalytics.model.MacroIndicator;
import com.calemccammon.financeanalytics.model.MacroVsStocks;
import com.google.cloud.bigquery.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class MacroService {

    private final BigQuery bigQuery;
    private final String projectId;
    private final String dataset;

    public MacroService(BigQuery bigQuery,
                        @Value("${bigquery.project-id}") String projectId,
                        @Value("${bigquery.dataset}") String dataset) {
        this.bigQuery = bigQuery;
        this.projectId = projectId;
        this.dataset = dataset;
    }

    public List<MacroIndicator> getIndicators() {
        // Filter to a single ticker to deduplicate macro dates (same approach as the dashboard).
        String sql = """
                SELECT
                    trading_date,
                    fed_funds_rate          / 100.0 AS fed_funds_rate,
                    cpi,
                    unemployment_rate       / 100.0 AS unemployment_rate,
                    treasury_yield_10y      / 100.0 AS treasury_yield_10y,
                    gdp_growth_rate         / 100.0 AS gdp_growth_rate,
                    breakeven_inflation_10y / 100.0 AS breakeven_inflation_10y,
                    real_yield_10y          / 100.0 AS real_yield_10y
                FROM `%s.%s.fct_daily_trading`
                WHERE ticker = 'AAPL'
                ORDER BY trading_date
                """.formatted(projectId, dataset);

        return query(sql, row -> new MacroIndicator(
                LocalDate.parse(row.get("trading_date").getStringValue()),
                nullable(row, "fed_funds_rate"),
                nullable(row, "cpi"),
                nullable(row, "unemployment_rate"),
                nullable(row, "treasury_yield_10y"),
                nullable(row, "gdp_growth_rate"),
                nullable(row, "breakeven_inflation_10y"),
                nullable(row, "real_yield_10y")));
    }

    public List<MacroVsStocks> getVsStocks() {
        // Returns daily return statistics aggregated by Federal Funds Rate regime.
        // Rate thresholds match the dashboard's CASE WHEN logic (applied to the raw
        // whole-percentage value stored in BigQuery, e.g. 5.0 = 5%).
        String sql = """
                WITH regimes AS (
                    SELECT
                        CASE
                            WHEN fed_funds_rate >= 5.0 THEN 'High Rates (>=5%%)'
                            WHEN fed_funds_rate >= 3.0 THEN 'Elevated Rates (3-5%%)'
                            WHEN fed_funds_rate >= 1.0 THEN 'Normal Rates (1-3%%)'
                            ELSE 'Low Rates (<1%%)'
                        END AS rate_regime,
                        daily_return_pct / 100.0 AS daily_return_pct
                    FROM `%s.%s.fct_daily_trading`
                    WHERE fed_funds_rate IS NOT NULL
                )
                SELECT
                    rate_regime,
                    ROUND(AVG(daily_return_pct), 6)    AS avg_daily_return_pct,
                    ROUND(STDDEV(daily_return_pct), 6) AS std_dev_pct,
                    COUNT(*)                            AS observations
                FROM regimes
                GROUP BY rate_regime
                ORDER BY AVG(daily_return_pct) DESC
                """.formatted(projectId, dataset);

        return query(sql, row -> new MacroVsStocks(
                row.get("rate_regime").getStringValue(),
                nullable(row, "avg_daily_return_pct"),
                nullable(row, "std_dev_pct"),
                (long) row.get("observations").getDoubleValue()));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private <T> List<T> query(String sql, RowMapper<T> mapper) {
        try {
            QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql)
                    .setUseLegacySql(false)
                    .build();
            TableResult result = bigQuery.query(config);
            return StreamSupport.stream(result.iterateAll().spliterator(), false)
                    .map(mapper::map)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("BigQuery query was interrupted", e);
        }
    }

    private Double nullable(FieldValueList row, String field) {
        FieldValue fv = row.get(field);
        return fv.isNull() ? null : fv.getDoubleValue();
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(FieldValueList row);
    }
}
