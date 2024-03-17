package edu.eflerrr.scrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DirtiesContext
class MigrationTest extends IntegrationTest {

    @Test
    public void allTablesInitTest() {
        try (Connection connection = DriverManager.getConnection(
            POSTGRES.getJdbcUrl(),
            POSTGRES.getUsername(),
            POSTGRES.getPassword()
        )
        ) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
            );
            List<String> tableNames = new ArrayList<>();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("table_name"));
            }

            assertThat(tableNames)
                .hasSize(6)
                .containsExactlyInAnyOrder(
                    "Chat", "Link", "Tracking", "Branch", "databasechangelog", "databasechangeloglock"
                );
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}
