package ru.vyatsu;

import org.flywaydb.core.Flyway;
import ru.vyatsu.db.DBConfig;

public class MigrationRunner {
    public static void main(String[] args) {
        Flyway flyway = Flyway.configure()
                .dataSource(DBConfig.URL, DBConfig.USER ,DBConfig.PASSWORD)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        System.out.println("✅ Миграции выполнены!");
    }
}
