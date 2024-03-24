package edu.eflerrr.scrapper.domain.jdbc.dao;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.jdbc.dto.Branch;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = {
    "classpath:scripts/clearLinkTable.sql",
    "classpath:scripts/clearBranchTable.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class BranchDaoTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BranchDao branchDao;
    private Long linkId;
    private final OffsetDateTime staticDateTime = OffsetDateTime.of(
        2021, 1, 1,
        0, 0, 0,
        0, ZoneOffset.UTC
    );

    @Nested
    class AddTest {

        @Test
        public void addOneBranchTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            var branch = new Branch(
                linkId,
                "TestOwner",
                "TestRepo",
                "TestBranch",
                staticDateTime
            );

            branchDao.add(branch);
            var actualBranches = jdbcTemplate.query("SELECT * FROM \"Branch\"", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
                    rs.getLong("link_id"),
                    rs.getString("repository_owner"),
                    rs.getString("repository_name"),
                    rs.getString("branch_name"),
                    rs.getObject("last_commit_time", OffsetDateTime.class)
                )
            );

            assertThat(actualBranches)
                .hasSize(1);
            assertThat(actualBranches.getFirst().getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranches.getFirst().getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranches.getFirst().getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranches.getFirst().getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranches.getFirst().getLastCommitTime())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void addSomeBranchesTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            var branch1 = new Branch(
                linkId, "first", "1", "uno", staticDateTime
            );
            var branch2 = new Branch(
                linkId, "second", "2", "dos", staticDateTime
            );
            var branch3 = new Branch(
                linkId, "third", "3", "tres", staticDateTime
            );

            branchDao.add(branch1);
            branchDao.add(branch2);
            branchDao.add(branch3);
            var actualBranches = jdbcTemplate.query("SELECT * FROM \"Branch\"", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
                    rs.getLong("link_id"),
                    rs.getString("repository_owner"),
                    rs.getString("repository_name"),
                    rs.getString("branch_name"),
                    rs.getObject("last_commit_time", OffsetDateTime.class)
                )
            );
            var actualBranch1 = actualBranches.get(0);
            var actualBranch2 = actualBranches.get(1);
            var actualBranch3 = actualBranches.get(2);

            assertThat(actualBranches)
                .hasSize(3);

            assertThat(actualBranch1.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch1.getRepositoryOwner())
                .isEqualTo("first");
            assertThat(actualBranch1.getRepositoryName())
                .isEqualTo("1");
            assertThat(actualBranch1.getBranchName())
                .isEqualTo("uno");
            assertThat(actualBranch1.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch2.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch2.getRepositoryOwner())
                .isEqualTo("second");
            assertThat(actualBranch2.getRepositoryName())
                .isEqualTo("2");
            assertThat(actualBranch2.getBranchName())
                .isEqualTo("dos");
            assertThat(actualBranch2.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch3.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch3.getRepositoryOwner())
                .isEqualTo("third");
            assertThat(actualBranch3.getRepositoryName())
                .isEqualTo("3");
            assertThat(actualBranch3.getBranchName())
                .isEqualTo("tres");
            assertThat(actualBranch3.getLastCommitTime())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void addBranchWithNullIdTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            var invalidBranch = new Branch(
                linkId, "first", null, "uno", staticDateTime
            );

            assertThatThrownBy(() -> branchDao.add(invalidBranch))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    class DeleteTest {

        @Test
        public void deleteOneBranchTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            var branch = new Branch(
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            branchDao.delete(branch);
            var actualBranches = jdbcTemplate.query("SELECT * FROM \"Branch\"", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
                    rs.getLong("link_id"),
                    rs.getString("repository_owner"),
                    rs.getString("repository_name"),
                    rs.getString("branch_name"),
                    rs.getObject("last_commit_time", OffsetDateTime.class)
                )
            );

            assertThat(actualBranches)
                .isEmpty();
        }

        @Test
        public void deleteSomeBranchesTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch3", staticDateTime
            );
            var branch1 = new Branch(
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            var branch3 = new Branch(
                linkId, "TestOwner", "TestRepo", "TestBranch3", staticDateTime
            );

            branchDao.delete(branch1);
            branchDao.delete(branch3);
            var actualBranches = jdbcTemplate.query("SELECT * FROM \"Branch\"", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
                    rs.getLong("link_id"),
                    rs.getString("repository_owner"),
                    rs.getString("repository_name"),
                    rs.getString("branch_name"),
                    rs.getObject("last_commit_time", OffsetDateTime.class)
                )
            );

            assertThat(actualBranches)
                .hasSize(1);
            assertThat(actualBranches.getFirst().getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranches.getFirst().getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranches.getFirst().getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranches.getFirst().getBranchName())
                .isEqualTo("TestBranch2");
            assertThat(actualBranches.getFirst().getLastCommitTime())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void deleteNonExistentBranchTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            var nonExistingBranch = new Branch(
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            assertThatThrownBy(() -> branchDao.delete(nonExistingBranch))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Branch not found!");
        }
    }

    @Nested
    class FindAllTest {

        @Test
        public void findAllEmptyTest() {
            var actualBranches = branchDao.findAll();

            assertThat(actualBranches)
                .isEmpty();
        }

        @Test
        public void findAllOneBranchTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualBranches = branchDao.findAll();

            assertThat(actualBranches)
                .hasSize(1);
            assertThat(actualBranches.getFirst().getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranches.getFirst().getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranches.getFirst().getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranches.getFirst().getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranches.getFirst().getLastCommitTime())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void findAllSomeBranchesTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch3", staticDateTime
            );

            var actualBranches = branchDao.findAll();
            var actualBranch1 = actualBranches.get(0);
            var actualBranch2 = actualBranches.get(1);
            var actualBranch3 = actualBranches.get(2);

            assertThat(actualBranches)
                .hasSize(3);

            assertThat(actualBranch1.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch1.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch1.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch1.getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranch1.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch2.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch2.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch2.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch2.getBranchName())
                .isEqualTo("TestBranch2");
            assertThat(actualBranch2.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch3.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch3.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch3.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch3.getBranchName())
                .isEqualTo("TestBranch3");
            assertThat(actualBranch3.getLastCommitTime())
                .isEqualTo(staticDateTime);
        }
    }

    @Nested
    class ExistsTest {

        @Test
        public void existsBranchFalseTest() {
            var branch = new Branch(
                1L, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualResult = branchDao.exists(branch);

            assertThat(actualResult)
                .isFalse();
        }

        @Test
        public void existsBranchTrueTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            var branch = new Branch(
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualResult = branchDao.exists(branch);

            assertThat(actualResult)
                .isTrue();
        }
    }

    @SuppressWarnings("SqlWithoutWhere") @Nested
    class FindAllByOwnerAndNameTest {

        @Test
        public void findAllByOwnerAndNameEmptyTest() {
            var actualBranches = branchDao.findAllByOwnerAndName("TestOwner", "TestRepo");

            assertThat(actualBranches)
                .isEmpty();
        }

        @Test
        public void findAllByOwnerAndNameOneBranchTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualBranches = branchDao.findAllByOwnerAndName("TestOwner", "TestRepo");

            assertThat(actualBranches)
                .hasSize(1);
            assertThat(actualBranches.getFirst().getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranches.getFirst().getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranches.getFirst().getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranches.getFirst().getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranches.getFirst().getLastCommitTime())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void findAllByOwnerAndNameSomeBranchesTest() {
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://test.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://test.ru"
            );

            String sql = """
                INSERT INTO "Branch" (link_id, repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "TestOwner", "AnotherTestRepo", "SomeTestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                linkId, "AnotherTestOwner", "TestRepo", "AnotherTestBranch", staticDateTime
            );

            var actualBranches = branchDao.findAllByOwnerAndName("TestOwner", "TestRepo");
            var actualBranch1 = actualBranches.getFirst();
            var actualBranch2 = actualBranches.getLast();

            assertThat(actualBranches)
                .hasSize(2);

            assertThat(actualBranch1.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch1.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch1.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch1.getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranch1.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch2.getLinkId())
                .isEqualTo(linkId);
            assertThat(actualBranch2.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch2.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch2.getBranchName())
                .isEqualTo("TestBranch2");
            assertThat(actualBranch2.getLastCommitTime())
                .isEqualTo(staticDateTime);
        }
    }

}
