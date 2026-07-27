package com.finance.tracker;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@EnableScheduling
@SpringBootApplication
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    @Bean
    CommandLineRunner verifyDataSource(DataSource dataSource,
                                       JdbcTemplate jdbcTemplate) {
        return args -> {

            System.out.println("====================================");

            if (dataSource instanceof HikariDataSource hikari) {
                System.out.println("JDBC URL : " + hikari.getJdbcUrl());
                System.out.println("Username : " + hikari.getUsername());
            }

            System.out.println("Current DB : "
                    + jdbcTemplate.queryForObject(
                    "select current_database()",
                    String.class));

            System.out.println("====================================");
        };
    }
}
