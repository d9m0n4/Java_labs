package ru.vyatsu;

import org.flywaydb.core.Flyway;

public class MigrationRunner {
    public static void main(String[] args) {
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:5432/autoparts_db", "postgres", "root")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        System.out.println("✅ Миграции выполнены!");
    }
}
