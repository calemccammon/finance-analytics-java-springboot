package com.calemccammon.financeanalytics.config;

import com.google.cloud.bigquery.BigQuery;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Proxy;

/**
 * Stub BigQuery bean for the "docs" Spring profile.
 * The app starts without GCP credentials so the OpenAPI spec can be generated
 * in CI. No controller method is actually invoked during spec generation.
 */
@Configuration
@Profile("docs")
public class BigQueryDocsConfig {

    @Bean
    public BigQuery bigQuery() {
        return (BigQuery) Proxy.newProxyInstance(
                BigQuery.class.getClassLoader(),
                new Class[]{BigQuery.class},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(
                            "BigQuery stub — not available in docs profile");
                });
    }
}
