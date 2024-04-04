package edu.eflerrr.scrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DirtiesContext
public abstract class IntegrationTest {
    public static PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");
        POSTGRES.start();

        try {
            runMigrations(POSTGRES);
        } catch (SQLException | LiquibaseException | FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void runMigrations(JdbcDatabaseContainer<?> c)
        throws SQLException, LiquibaseException, FileNotFoundException {
        Path changelogPath = new File(".").toPath().toAbsolutePath()
            .getParent().getParent();
        changelogPath = changelogPath.resolve("migrations");

        try (Connection connection = DriverManager.getConnection(
            c.getJdbcUrl(),
            c.getUsername(),
            c.getPassword()
        )) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new JdbcConnection(connection)
            );

            Liquibase liquibase = new Liquibase(
                "changelog-master.xml",
                new DirectoryResourceAccessor(changelogPath),
                database
            );
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }

    @DynamicPropertySource
    static void jdbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @DynamicPropertySource
    static void disableMessageQueue(DynamicPropertyRegistry registry) {
        registry.add("app.use-queue", () -> false);
    }

}
