package edu.eflerrr.scrapper.domain.dao;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.dto.Branch;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BranchDaoTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BranchDao branchDao;
    private final OffsetDateTime staticDateTime = OffsetDateTime.of(
        2021, 1, 1,
        0, 0, 0,
        0, ZoneOffset.UTC
    );

    @Nested
    class AddTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Branch");
        }

        @Test
        public void addOneBranchTest() {
            var branch = new Branch(
                "TestOwner",
                "TestRepo",
                "TestBranch",
                staticDateTime
            );

            branchDao.add(branch);
            var actualBranches = jdbcTemplate.query("SELECT * FROM Branch", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
                    rs.getString("repository_owner"),
                    rs.getString("repository_name"),
                    rs.getString("branch_name"),
                    rs.getObject("last_commit_time", OffsetDateTime.class)
                )
            );

            assertThat(actualBranches)
                .hasSize(1);
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
            var branch1 = new Branch(
                "first", "1", "uno", staticDateTime
            );
            var branch2 = new Branch(
                "second", "2", "dos", staticDateTime
            );
            var branch3 = new Branch(
                "third", "3", "tres", staticDateTime
            );

            branchDao.add(branch1);
            branchDao.add(branch2);
            branchDao.add(branch3);
            var actualBranches = jdbcTemplate.query("SELECT * FROM Branch", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
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

            assertThat(actualBranch1.getRepositoryOwner())
                .isEqualTo("first");
            assertThat(actualBranch1.getRepositoryName())
                .isEqualTo("1");
            assertThat(actualBranch1.getBranchName())
                .isEqualTo("uno");
            assertThat(actualBranch1.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch2.getRepositoryOwner())
                .isEqualTo("second");
            assertThat(actualBranch2.getRepositoryName())
                .isEqualTo("2");
            assertThat(actualBranch2.getBranchName())
                .isEqualTo("dos");
            assertThat(actualBranch2.getLastCommitTime())
                .isEqualTo(staticDateTime);

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
            var invalidBranch = new Branch(
                "first", null, "uno", staticDateTime
            );

            assertThatThrownBy(() -> branchDao.add(invalidBranch))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    class DeleteTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Branch");
        }

        @Test
        public void deleteOneBranchTest() {
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            var branch = new Branch(
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            branchDao.delete(branch);
            var actualBranches = jdbcTemplate.query("SELECT * FROM Branch", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
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
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            jdbcTemplate.update(
                sql, "TestOwner", "TestRepo", "TestBranch3", staticDateTime
            );
            var branch1 = new Branch(
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            var branch2 = new Branch(
                "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            var branch3 = new Branch(
                "TestOwner", "TestRepo", "TestBranch3", staticDateTime
            );

            branchDao.delete(branch1);
            branchDao.delete(branch3);
            var actualBranches = jdbcTemplate.query("SELECT * FROM Branch", (rs, rowNum) ->
                new Branch(
                    rs.getLong("id"),
                    rs.getString("repository_owner"),
                    rs.getString("repository_name"),
                    rs.getString("branch_name"),
                    rs.getObject("last_commit_time", OffsetDateTime.class)
                )
            );

            assertThat(actualBranches)
                .hasSize(1);
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
            var nonExistingBranch = new Branch(
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            assertThatThrownBy(() -> branchDao.delete(nonExistingBranch))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Branch not found!");
        }
    }

    @Nested
    class FindAllTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Branch");
        }

        @Test
        public void findAllEmptyTest() {
            var actualBranches = branchDao.findAll();

            assertThat(actualBranches)
                .isEmpty();
        }

        @Test
        public void findAllOneBranchTest() {
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualBranches = branchDao.findAll();

            assertThat(actualBranches)
                .hasSize(1);
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
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            jdbcTemplate.update(
                sql, "TestOwner", "TestRepo", "TestBranch3", staticDateTime
            );

            var actualBranches = branchDao.findAll();
            var actualBranch1 = actualBranches.get(0);
            var actualBranch2 = actualBranches.get(1);
            var actualBranch3 = actualBranches.get(2);

            assertThat(actualBranches)
                .hasSize(3);

            assertThat(actualBranch1.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch1.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch1.getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranch1.getLastCommitTime())
                .isEqualTo(staticDateTime);

            assertThat(actualBranch2.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch2.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch2.getBranchName())
                .isEqualTo("TestBranch2");
            assertThat(actualBranch2.getLastCommitTime())
                .isEqualTo(staticDateTime);

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

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Branch");
        }

        @Test
        public void existsBranchFalseTest() {
            var branch = new Branch(
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualResult = branchDao.exists(branch);

            assertThat(actualResult)
                .isFalse();
        }

        @Test
        public void existsBranchTrueTest() {
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            var branch = new Branch(
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualResult = branchDao.exists(branch);

            assertThat(actualResult)
                .isTrue();
        }
    }

    @Nested
    class FindAllByOwnerAndNameTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Branch");
        }

        @Test
        public void findAllByOwnerAndNameEmptyTest() {
            var actualBranches = branchDao.findAllByOwnerAndName("TestOwner", "TestRepo");

            assertThat(actualBranches)
                .isEmpty();
        }

        @Test
        public void findAllByOwnerAndNameOneBranchTest() {
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );

            var actualBranches = branchDao.findAllByOwnerAndName("TestOwner", "TestRepo");

            assertThat(actualBranches)
                .hasSize(1);
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
        public void findAllByOwnerAndNameSomeBranchesTest() {
            String sql = """
                INSERT INTO Branch (repository_owner, repository_name, branch_name, last_commit_time)\s
                VALUES (?, ?, ?, ?)
                """;
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch", staticDateTime
            );
            jdbcTemplate.update(sql,
                "TestOwner", "TestRepo", "TestBranch2", staticDateTime
            );
            jdbcTemplate.update(sql,
                "TestOwner", "AnotherTestRepo", "SomeTestBranch", staticDateTime
            );
            jdbcTemplate.update(
                sql, "AnotherTestOwner", "TestRepo", "AnotherTestBranch", staticDateTime
            );

            var actualBranches = branchDao.findAllByOwnerAndName("TestOwner", "TestRepo");
            var actualBranch1 = actualBranches.getFirst();
            var actualBranch2 = actualBranches.getLast();

            assertThat(actualBranches)
                .hasSize(2);

            assertThat(actualBranch1.getRepositoryOwner())
                .isEqualTo("TestOwner");
            assertThat(actualBranch1.getRepositoryName())
                .isEqualTo("TestRepo");
            assertThat(actualBranch1.getBranchName())
                .isEqualTo("TestBranch");
            assertThat(actualBranch1.getLastCommitTime())
                .isEqualTo(staticDateTime);

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
