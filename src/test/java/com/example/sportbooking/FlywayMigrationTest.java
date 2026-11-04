package com.example.sportbooking;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

public class FlywayMigrationTest {

    @Test
    void runFlywayAgainstMysql() {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource("jdbc:mysql://localhost:3309/sport_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                            "root", "phanna#5")
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
            System.out.println("FLYWAY MIGRATION AND SEEDING SUCCESSFUL ON MYSQL!");
        } catch (Exception e) {
            System.err.println("Flyway migration exception: " + e.getMessage());
            throw e;
        }
    }
}
