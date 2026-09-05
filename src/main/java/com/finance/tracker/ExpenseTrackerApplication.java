package com.finance.tracker;


import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@EnableScheduling
@EnableAspectJAutoProxy
@SpringBootApplication
@Slf4j
public class ExpenseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    @Bean
    CommandLineRunner verifyDataSource(DataSource dataSource,
                                       JdbcTemplate jdbcTemplate) {
        return args -> {
            if (dataSource instanceof HikariDataSource hikari) {
                log.info("DB connected: {} | user = {}", hikari.getJdbcUrl(), hikari.getUsername());
            }
            String db = jdbcTemplate.queryForObject("select current_database()", String.class);
            log.info("Active Database : {}", db);

        };
    }
}
