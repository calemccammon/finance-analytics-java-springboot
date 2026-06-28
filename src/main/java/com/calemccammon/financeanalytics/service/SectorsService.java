package com.calemccammon.financeanalytics.service;

import com.calemccammon.financeanalytics.model.SectorSummary;
import com.google.cloud.bigquery.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class SectorsService {

    private final BigQuery bigQuery;
    private final String projectId;
    private final String dataset;

    public SectorsService(BigQuery bigQuery,
                          @Value("${bigquery.project-id}") String projectId,
                          @Value("${bigquery.dataset}") String dataset) {
        this.bigQuery = bigQuery;
        this.projectId = projectId;
        this.dataset = dataset;
    }

    public List<SectorSummary> getSummary() {
        String sql = """
                SELECT
                    sector,
                    COUNT(DISTINCT ticker)                                     AS stock_count,
                    ROUND(AVG(daily_return_pct) / 100.0, 6)                   AS avg_daily_return_pct,
                    ROUND(AVG(volatility_20d_annualized) / 100.0, 4)          AS avg_volatility_20d_pct,
                    ROUND(AVG(volume), 0)                                      AS avg_volume,
                    ROUND(AVG(close_price / NULLIF(sma_200d, 0) - 1), 4)      AS avg_pct_above_200sma
                FROM `%s.%s.fct_daily_trading`
                WHERE trading_date = (
                    SELECT MAX(trading_date) FROM `%s.%s.fct_daily_trading`
                )
                GROUP BY sector
                ORDER BY avg_daily_return_pct DESC
                """.formatted(projectId, dataset, projectId, dataset);

        try {
            QueryJobConfiguration config = QueryJobConfiguration.newBuilder(sql)
                    .setUseLegacySql(false)
                    .build();
            TableResult result = bigQuery.query(config);
            return StreamSupport.stream(result.iterateAll().spliterator(), false)
                    .map(row -> new SectorSummary(
                            row.get("sector").getStringValue(),
                            (int) row.get("stock_count").getLongValue(),
                            nullable(row, "avg_daily_return_pct"),
                            nullable(row, "avg_volatility_20d_pct"),
                            nullable(row, "avg_volume"),
                            nullable(row, "avg_pct_above_200sma")))
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
}
