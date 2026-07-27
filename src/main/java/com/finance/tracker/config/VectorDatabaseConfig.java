package com.finance.tracker.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class VectorDatabaseConfig {

    @Bean(name = "vectorDataSource")
    public DataSource vectorDataSource() {

        DriverManagerDataSource ds = new DriverManagerDataSource();

        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://localhost:5433/postgres");
        ds.setUsername("postgres");
        ds.setPassword("postgres");
        System.out.println("Vector URL = " + ds.getUrl());

        return ds;
    }

    @Bean
    PgVectorStore vectorStore(
            JdbcTemplate vectorJdbcTemplate,
            EmbeddingModel embeddingModel) {

        return PgVectorStore.builder(vectorJdbcTemplate, embeddingModel)
                .initializeSchema(true)
                .dimensions(1024)
                .build();
    }

    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate(
            @Qualifier("vectorDataSource")
            DataSource ds) {

        return new JdbcTemplate(ds);
    }
}
