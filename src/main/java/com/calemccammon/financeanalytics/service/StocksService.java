package com.calemccammon.financeanalytics.service;

import com.calemccammon.financeanalytics.model.MonthlyReturn;
import com.calemccammon.financeanalytics.model.Performance52w;
import com.calemccammon.financeanalytics.model.StockSnapshot;
import com.google.cloud.bigquery.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Service
public class StocksService {

    private final BigQuery bigQuery;
    private final String projectId;
    private final String dataset;

    public StocksService(BigQuery bigQuery,
                         @Value("${bigquery.project-id}") String projectId,
                         @Value("${bigquery.dataset}") String dataset) {
        this.bigQuery = bigQuery;
        this.projectId = projectId;
        this.dataset = dataset;
    }

    public List<StockSnapshot> getSnapshot() {
        String sql = """
                SELECT
                    f.ticker,
                    f.company_name,
                    f.sector,
                    c.industry,
                    c.market_cap_category,
                    c.exchange,
                    f.close_price                                AS latest_close,
                    f.daily_return_pct / 100.0                  AS daily_return_pct,
                    f.volume,
                    f.sma_30d,
                    f.sma_200d,
                    ROUND(f.volatility_20d_annualized / 100.0, 4) AS volatility_20d_pct,
                    f.trading_date                              AS as_of_date
                FROM `%s.%s.fct_daily_trading` f
                JOIN `%s.%s.dim_company` c USING (ticker)
                WHERE f.trading_date = (
                    SELECT MAX(trading_date) FROM `%s.%s.fct_daily_trading`
                )
                ORDER BY f.company_name
                """.formatted(projectId, dataset, projectId, dataset, projectId, dataset);

        return query(sql, row -> new StockSnapshot(
                row.get("ticker").getStringValue(),
                row.get("company_name").getStringValue(),
                row.get("sector").getStringValue(),
                row.get("industry").getStringValue(),
                row.get("market_cap_category").getStringValue(),
                row.get("exchange").getStringValue(),
                nullable(row, "latest_close"),
                nullable(row, "daily_return_pct"),
                (long) row.get("volume").getDoubleValue(),
                nullable(row, "sma_30d"),
                nullable(row, "sma_200d"),
                nullable(row, "volatility_20d_pct"),
                LocalDate.parse(row.get("as_of_date").getStringValue())));
    }

    public List<Performance52w> getPerformance() {
        String sql = """
                WITH ranked AS (
                    SELECT
                        ticker, company_name, sector, close_price,
                        volatility_20d_annualized, volume,
                        ROW_NUMBER() OVER (PARTITION BY ticker ORDER BY trading_date ASC)  AS rn_asc,
                        ROW_NUMBER() OVER (PARTITION BY ticker ORDER BY trading_date DESC) AS rn_desc
                    FROM `%s.%s.fct_daily_trading`
                    WHERE trading_date >= DATE_SUB(
                        (SELECT MAX(trading_date) FROM `%s.%s.fct_daily_trading`),
                        INTERVAL 365 DAY
                    )
                )
                SELECT
                    ticker,
                    company_name,
                    sector,
                    MAX(CASE WHEN rn_asc  = 1 THEN close_price END) AS price_52w_ago,
                    MAX(CASE WHEN rn_desc = 1 THEN close_price END) AS price_now,
                    ROUND(
                        MAX(CASE WHEN rn_desc = 1 THEN close_price END) /
                        MAX(CASE WHEN rn_asc  = 1 THEN close_price END) - 1, 4
                    ) AS return_52w_pct,
                    ROUND(AVG(volatility_20d_annualized) / 100.0, 4) AS avg_volatility_pct,
                    ROUND(AVG(volume), 0)                              AS avg_daily_volume
                FROM ranked
                GROUP BY ticker, company_name, sector
                ORDER BY return_52w_pct DESC
                """.formatted(projectId, dataset, projectId, dataset);

        return query(sql, row -> new Performance52w(
                row.get("ticker").getStringValue(),
                row.get("company_name").getStringValue(),
                row.get("sector").getStringValue(),
                nullable(row, "price_52w_ago"),
                nullable(row, "price_now"),
                nullable(row, "return_52w_pct"),
                nullable(row, "avg_volatility_pct"),
                nullable(row, "avg_daily_volume")));
    }

    public List<MonthlyReturn> getMonthlyReturns(String ticker) {
        String sql = """
                WITH ranked AS (
                    SELECT
                        ticker,
                        close_price,
                        DATE_TRUNC(trading_date, MONTH) AS month_start,
                        ROW_NUMBER() OVER (
                            PARTITION BY ticker, DATE_TRUNC(trading_date, MONTH)
                            ORDER BY trading_date ASC
                        ) AS rn_asc,
                        ROW_NUMBER() OVER (
                            PARTITION BY ticker, DATE_TRUNC(trading_date, MONTH)
                            ORDER BY trading_date DESC
                        ) AS rn_desc,
                        COUNT(*) OVER (
                            PARTITION BY ticker, DATE_TRUNC(trading_date, MONTH)
                        ) AS trading_days
                    FROM `%s.%s.fct_daily_trading`
                    WHERE ticker = @ticker
                )
                SELECT
                    ticker,
                    month_start                                          AS month,
                    ROUND(
                        MAX(CASE WHEN rn_desc = 1 THEN close_price END) /
                        MAX(CASE WHEN rn_asc  = 1 THEN close_price END) - 1, 4
                    ) AS monthly_return_pct,
                    MAX(trading_days)                                    AS trading_days
                FROM ranked
                GROUP BY ticker, month_start
                ORDER BY month_start DESC
                """.formatted(projectId, dataset);

        QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql)
                .setUseLegacySql(false)
                .addNamedParameter("ticker", QueryParameterValue.string(ticker))
                .build();

        return query(config, row -> new MonthlyReturn(
                row.get("ticker").getStringValue(),
                LocalDate.parse(row.get("month").getStringValue()),
                nullable(row, "monthly_return_pct"),
                (int) row.get("trading_days").getDoubleValue()));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private <T> List<T> query(String sql, RowMapper<T> mapper) {
        return query(QueryJobConfiguration.newBuilder(sql).setUseLegacySql(false).build(), mapper);
    }

    private <T> List<T> query(QueryJobConfiguration config, RowMapper<T> mapper) {
        try {
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
