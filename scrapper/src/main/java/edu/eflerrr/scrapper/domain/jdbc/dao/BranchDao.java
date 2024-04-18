package edu.eflerrr.scrapper.domain.jdbc.dao;

import edu.eflerrr.scrapper.domain.jdbc.dto.Branch;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("MultipleStringLiterals")
public class BranchDao {

    private final JdbcTemplate jdbcTemplate;

    public boolean exists(Branch branch) {
        String countSql = """
            SELECT COUNT(*) AS row_count\s
            FROM "Branch"\s
            WHERE repository_owner = ? AND repository_name = ? AND branch_name = ?
            """;
        var rowCount = jdbcTemplate.queryForObject(
            countSql,
            Integer.class,
            branch.getRepositoryOwner(),
            branch.getRepositoryName(),
            branch.getBranchName()
        );
        return rowCount != null && rowCount == 1;
    }

    public void add(Branch branch) {
        String sql = """
            INSERT INTO "Branch" (repository_owner, repository_name, branch_name, last_commit_time)\s
            VALUES (?, ?, ?, ?)
            """;
        jdbcTemplate.update(
            sql,
            branch.getRepositoryOwner(),
            branch.getRepositoryName(),
            branch.getBranchName(),
            branch.getLastCommitTime()
        );
    }

    public void delete(Branch branch) {
        if (!exists(branch)) {
            throw new InvalidDataAccessResourceUsageException("Branch not found!");
        }

        String sql = """
                DELETE FROM "Branch"\s
                WHERE repository_owner = ? AND repository_name = ? AND branch_name = ?
            """;
        jdbcTemplate.update(
            sql,
            branch.getRepositoryOwner(),
            branch.getRepositoryName(),
            branch.getBranchName()
        );
    }

    public List<Branch> findAll() {
        String sql = "SELECT * FROM \"Branch\"";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Branch(
                rs.getLong("id"),
                rs.getString("repository_owner"),
                rs.getString("repository_name"),
                rs.getString("branch_name"),
                rs.getObject("last_commit_time", OffsetDateTime.class)
            )
        );
    }

    public List<Branch> findAllByOwnerAndName(String owner, String name) {
        String sql = "SELECT * FROM \"Branch\" WHERE repository_owner = ? AND repository_name = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Branch(
                rs.getLong("id"),
                rs.getString("repository_owner"),
                rs.getString("repository_name"),
                rs.getString("branch_name"),
                rs.getObject("last_commit_time", OffsetDateTime.class)
            ), owner, name
        );
    }

}
