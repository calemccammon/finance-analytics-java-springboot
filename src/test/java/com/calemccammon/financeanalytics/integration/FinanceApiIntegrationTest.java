package com.calemccammon.financeanalytics.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that call the real BigQuery dataset.
 *
 * These tests require GCP credentials and are only executed when BQ_PROJECT is set.
 * They run in CI via integration-test.yml on push to main.
 *
 * Run locally:
 *   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json
 *   export BQ_PROJECT=your-project-id
 *   mvn test -Dgroups=integration
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "BQ_PROJECT", matches = ".+")
class FinanceApiIntegrationTest {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void stocksSnapshot_returnsNonEmptyList() {
        ResponseEntity<List> response = rest.getForEntity(url("/api/stocks/snapshot"), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void stocksPerformance_returnsNonEmptyList() {
        ResponseEntity<List> response = rest.getForEntity(url("/api/stocks/performance"), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void stocksMonthlyReturns_returnsNonEmptyListForAAPL() {
        ResponseEntity<List> response = rest.getForEntity(url("/api/stocks/AAPL/monthly-returns"), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void macroIndicators_returnsNonEmptyList() {
        ResponseEntity<List> response = rest.getForEntity(url("/api/macro/indicators"), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void macroVsStocks_returnsNonEmptyList() {
        ResponseEntity<List> response = rest.getForEntity(url("/api/macro/vs-stocks"), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void sectorsSummary_returnsNonEmptyList() {
        ResponseEntity<List> response = rest.getForEntity(url("/api/sectors/summary"), List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void actuatorHealth_returnsUp() {
        ResponseEntity<String> response = rest.getForEntity(url("/actuator/health"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
