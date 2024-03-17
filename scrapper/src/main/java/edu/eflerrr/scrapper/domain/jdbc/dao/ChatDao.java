package edu.eflerrr.scrapper.domain.jdbc.dao;

import edu.eflerrr.scrapper.domain.jdbc.dto.Chat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ChatDao {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean exists(Chat chat) {
        String countSql = "SELECT COUNT(*) AS row_count FROM \"Chat\" WHERE id = ? AND username = ?";
        var rowCount = jdbcTemplate.queryForObject(countSql, Integer.class, chat.getId(), chat.getUsername());
        return rowCount != null && rowCount == 1;
    }

    @Transactional
    public void add(Chat chat) {
        String sql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, chat.getId(), chat.getUsername(),
            OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)
        );
    }

    @Transactional
    public void delete(Chat chat) {
        if (!exists(chat)) {
            throw new InvalidDataAccessResourceUsageException("Chat not found!");
        }

        String sql = "DELETE FROM \"Chat\" WHERE id = ? AND username = ?";
        jdbcTemplate.update(sql, chat.getId(), chat.getUsername());
    }

    @Transactional
    public List<Chat> findAll() {
        String sql = "SELECT * FROM \"Chat\"";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Chat(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getObject("created_at", OffsetDateTime.class)
            )
        );
    }

}
