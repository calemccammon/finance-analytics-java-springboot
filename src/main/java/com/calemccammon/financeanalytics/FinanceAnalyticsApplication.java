package com.calemccammon.financeanalytics;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Finance Analytics API",
        version = "1.0.0",
        description = """
            Read-only REST API over the finance_analytics BigQuery dataset.
            Data sourced from Yahoo Finance (stock prices) and FRED (macro indicators),
            transformed by dbt, and served from BigQuery.

            **To use "Try it out":** run the app locally with your GCP credentials,
            then return here and execute requests against http://localhost:8080.
            """
    )
)
public class FinanceAnalyticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceAnalyticsApplication.class, args);
    }
}
