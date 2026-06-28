package com.calemccammon.financeanalytics.config;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!docs")
public class BigQueryConfig {

    @Bean
    @ConditionalOnMissingBean
    public BigQuery bigQuery(@Value("${bigquery.project-id}") String projectId) {
        return BigQueryOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
    }
}
