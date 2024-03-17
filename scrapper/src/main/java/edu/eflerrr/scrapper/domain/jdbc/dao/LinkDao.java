package edu.eflerrr.scrapper.domain.jdbc.dao;

import edu.eflerrr.scrapper.domain.jdbc.dto.Link;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("MultipleStringLiterals")
public class LinkDao {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean exists(Link link) {
        if (link == null || link.getUrl() == null) {
            throw new NullPointerException("Link or URL is null!");
        }
        String countSql = "SELECT COUNT(*) AS row_count FROM \"Link\" WHERE url = ?";
        var rowCount = jdbcTemplate.queryForObject(countSql, Integer.class, link.getUrl().toString());
        return rowCount != null && rowCount == 1;
    }

    @Transactional
    public Long getId(Link link) {
        if (link == null || link.getUrl() == null) {
            throw new NullPointerException("Link or URL is null!");
        }
        String sql = "SELECT id FROM \"Link\" WHERE url = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, link.getUrl().toString());
        } catch (EmptyResultDataAccessException ex) {
            throw new InvalidDataAccessResourceUsageException("Link not found!");
        }
    }

    @Transactional
    public Link getLinkById(long id) {
        String sql = "SELECT * FROM \"Link\" WHERE id = ?";
        var linkList = jdbcTemplate.query(sql, (rs, rowNum) -> {
                try {
                    return new Link(
                        rs.getLong("id"),
                        new URI(rs.getString("url")),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("checked_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)
                    );
                } catch (URISyntaxException ex) {
                    throw new RuntimeException("Invalid URI in Database! Message: " + ex.getMessage());
                }
            }, id
        );
        if (linkList.isEmpty()) {
            throw new InvalidDataAccessResourceUsageException("Link not found!");
        } else {
            return linkList.getFirst();
        }
    }

    @Transactional
    public void add(Link link) {
        if (link == null || link.getUrl() == null) {
            throw new NullPointerException("Link or URL is null!");
        }
        String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
        var currentTime = OffsetDateTime.now();
        jdbcTemplate.update(sql, link.getUrl().toString(),
            currentTime.withOffsetSameInstant(ZoneOffset.UTC),
            link.getCheckedAt(),
            currentTime.withOffsetSameInstant(ZoneOffset.UTC)
        );
    }

    @Transactional
    public void delete(Link link) {
        if (link == null || link.getUrl() == null) {
            throw new NullPointerException("Link or URL is null!");
        }
        if (!exists(link)) {
            throw new InvalidDataAccessResourceUsageException("Link not found!");
        }

        String sql = "DELETE FROM \"Link\" WHERE url = ?";
        jdbcTemplate.update(sql, link.getUrl().toString());
    }

    @Transactional
    public List<Link> findAll() {
        String sql = "SELECT * FROM \"Link\"";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
                try {
                    return new Link(
                        rs.getLong("id"),
                        new URI(rs.getString("url")),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("checked_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)
                    );
                } catch (URISyntaxException ex) {
                    throw new RuntimeException("Invalid URI in Database! Message: " + ex.getMessage());
                }
            }
        );
    }

    @Transactional
    public List<Link> findAllWithFilter(Duration duration, OffsetDateTime currentTime) {
        String filterSql =
            "SELECT * FROM \"Link\" WHERE checked_at < (? - INTERVAL '" + duration.toSeconds() + " seconds')";
        return jdbcTemplate.query(filterSql, (rs, rowNum) -> {
                try {
                    return new Link(
                        rs.getLong("id"),
                        new URI(rs.getString("url")),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("checked_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)
                    );
                } catch (URISyntaxException ex) {
                    throw new RuntimeException("Invalid URI in Database! Message: " + ex.getMessage());
                }
            }, currentTime.withOffsetSameInstant(ZoneOffset.UTC)
        );
    }

    @Transactional
    public void updateCheckedAt(Link link, OffsetDateTime newCheckedAt) {
        if (link == null || link.getUrl() == null) {
            throw new NullPointerException("Link or URL is null!");
        }
        if (!exists(link)) {
            throw new InvalidDataAccessResourceUsageException("Link not found!");
        }

        String sql = "UPDATE \"Link\" SET checked_at = ? WHERE url = ?";
        jdbcTemplate.update(sql, newCheckedAt.withOffsetSameInstant(ZoneOffset.UTC), link.getUrl().toString());
    }

    @Transactional
    public void updateUpdatedAt(Link link, OffsetDateTime newUpdatedAt) {
        if (link == null || link.getUrl() == null) {
            throw new NullPointerException("Link or URL is null!");
        }
        if (!exists(link)) {
            throw new InvalidDataAccessResourceUsageException("Link not found!");
        }

        String sql = "UPDATE \"Link\" SET updated_at = ? WHERE url = ?";
        jdbcTemplate.update(sql, newUpdatedAt.withOffsetSameInstant(ZoneOffset.UTC), link.getUrl().toString());
    }

}
