package edu.eflerrr.scrapper.domain.dao;

import edu.eflerrr.scrapper.domain.dto.Tracking;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("MultipleStringLiterals")
public class TrackingDao {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean exists(Tracking tracking) {
        String countSql = "SELECT COUNT(*) AS row_count FROM Tracking WHERE chat_id = ? AND link_id = ?";
        var rowCount = jdbcTemplate.queryForObject(countSql, Integer.class, tracking.getChatId(), tracking.getLinkId());
        return rowCount != null && rowCount == 1;
    }

    @Transactional
    public void add(Tracking tracking) {
        if (exists(tracking)) {
            throw new InvalidDataAccessResourceUsageException("Tracking already exists!");
        }

        String sql = "INSERT INTO Tracking (chat_id, link_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, tracking.getChatId(), tracking.getLinkId());
    }

    @Transactional
    public void delete(Tracking tracking) {
        if (!exists(tracking)) {
            throw new InvalidDataAccessResourceUsageException("Tracking not found!");
        }

        String sql = "DELETE FROM Tracking WHERE chat_id = ? AND link_id = ?";
        jdbcTemplate.update(sql, tracking.getChatId(), tracking.getLinkId());
    }

    @Transactional
    public List<Tracking> findAll() {
        String sql = "SELECT * FROM Tracking";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Tracking(
                rs.getLong("id"),
                rs.getLong("chat_id"),
                rs.getLong("link_id")
            )
        );
    }

    @Transactional
    public List<Tracking> findAllByChatId(Long chatId) {
        String sql = "SELECT * FROM Tracking WHERE chat_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Tracking(
                rs.getLong("id"),
                rs.getLong("chat_id"),
                rs.getLong("link_id")
            ), chatId
        );
    }

    @Transactional
    public List<Tracking> findAllByLinkId(Long linkId) {
        String sql = "SELECT * FROM Tracking WHERE link_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Tracking(
                rs.getLong("id"),
                rs.getLong("chat_id"),
                rs.getLong("link_id")
            ), linkId
        );
    }

}
