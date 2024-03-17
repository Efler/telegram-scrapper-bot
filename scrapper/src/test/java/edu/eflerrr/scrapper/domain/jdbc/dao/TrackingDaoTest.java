package edu.eflerrr.scrapper.domain.jdbc.dao;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.jdbc.dto.Tracking;
import java.time.OffsetDateTime;
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
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TrackingDaoTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TrackingDao trackingDao;

    @Nested
    class AddTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM \"Tracking\"");
            jdbcTemplate.update("DELETE FROM \"Chat\"");
            jdbcTemplate.update("DELETE FROM \"Link\"");
        }

        @Test
        public void addOneTrackingTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 200L, "MeowMeowParrot", OffsetDateTime.now());
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
            var tracking = new Tracking(200L, linkId);

            trackingDao.add(tracking);
            var actualTrackings = jdbcTemplate.query("SELECT * FROM \"Tracking\"", (rs, rowNum) ->
                new Tracking(
                    rs.getLong("id"),
                    rs.getLong("chat_id"),
                    rs.getLong("link_id")
                )
            );

            assertThat(actualTrackings)
                .hasSize(1);
            assertThat(actualTrackings.getFirst().getChatId())
                .isEqualTo(200L);
            assertThat(actualTrackings.getFirst().getLinkId())
                .isEqualTo(linkId);
        }

        @Test
        public void addSomeTrackingsTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1111L, "speed", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 2222L, "agility", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 3333L, "movement", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://speed-news.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://agility-news.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId1 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://speed-news.ru"
            );
            var linkId2 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://agility-news.ru"
            );
            var tracking1 = new Tracking(1111L, linkId1);
            var tracking2 = new Tracking(2222L, linkId2);
            var tracking3 = new Tracking(3333L, linkId1);

            trackingDao.add(tracking1);
            trackingDao.add(tracking2);
            trackingDao.add(tracking3);
            var actualTrackings = jdbcTemplate.query("SELECT * FROM \"Tracking\"", (rs, rowNum) ->
                new Tracking(
                    rs.getLong("id"),
                    rs.getLong("chat_id"),
                    rs.getLong("link_id")
                )
            );
            var actualTracking1 = actualTrackings.get(0);
            var actualTracking2 = actualTrackings.get(1);
            var actualTracking3 = actualTrackings.get(2);

            assertThat(actualTrackings)
                .hasSize(3);

            assertThat(actualTracking1.getChatId())
                .isEqualTo(1111L);
            assertThat(actualTracking1.getLinkId())
                .isEqualTo(linkId1);

            assertThat(actualTracking2.getChatId())
                .isEqualTo(2222L);
            assertThat(actualTracking2.getLinkId())
                .isEqualTo(linkId2);

            assertThat(actualTracking3.getChatId())
                .isEqualTo(3333L);
            assertThat(actualTracking3.getLinkId())
                .isEqualTo(linkId1);
        }

        @Test
        public void addTrackingWithNullIdTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1111L, "Samurai-1337", OffsetDateTime.now());
            var invalidTracking = new Tracking(1111L, null);

            assertThatThrownBy(() -> trackingDao.add(invalidTracking))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        public void addSameTrackingTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1111L, "Samurai-1337", OffsetDateTime.now());
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
            var tracking1 = new Tracking(1111L, linkId);
            var tracking2 = new Tracking(1111L, linkId);
            trackingDao.add(tracking1);

            assertThatThrownBy(() -> trackingDao.add(tracking2))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class);
        }

        @Test
        public void addTrackingWithNonExistentLinkTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 5555L, "Samurai-1337", OffsetDateTime.now());
            var nonExistingLinkTracking = new Tracking(5555L, 6666L);

            assertThatThrownBy(() -> trackingDao.add(nonExistingLinkTracking))
                .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    class DeleteTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM \"Tracking\"");
            jdbcTemplate.update("DELETE FROM \"Chat\"");
            jdbcTemplate.update("DELETE FROM \"Link\"");
        }

        @Test
        public void deleteOneTrackingTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1234L, "TiredALot", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://1234-link.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://1234-link.ru"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 1234L, linkId);
            var tracking = new Tracking(1234L, linkId);

            trackingDao.delete(tracking);
            var actualTrackings = jdbcTemplate.query("SELECT * FROM \"Tracking\"", (rs, rowNum) ->
                new Tracking(
                    rs.getLong("id"),
                    rs.getLong("chat_id"),
                    rs.getLong("link_id")
                )
            );

            assertThat(actualTrackings)
                .isEmpty();
        }

        @Test
        public void deleteSomeTrackingsTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1234L, "MillyPops", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 5678L, "HeyHeyDoggy", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://pewpewpew.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://gogogo.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://lalala.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId1 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://pewpewpew.ru"
            );
            var linkId2 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://gogogo.ru"
            );
            var linkId3 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://lalala.ru"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 1234L, linkId1);
            jdbcTemplate.update(trackingSql, 5678L, linkId2);
            jdbcTemplate.update(trackingSql, 1234L, linkId3);
            var tracking1 = new Tracking(1234L, linkId1);
            var tracking3 = new Tracking(1234L, linkId3);

            trackingDao.delete(tracking1);
            trackingDao.delete(tracking3);
            var actualTrackings = jdbcTemplate.query("SELECT * FROM \"Tracking\"", (rs, rowNum) ->
                new Tracking(
                    rs.getLong("id"),
                    rs.getLong("chat_id"),
                    rs.getLong("link_id")
                )
            );

            assertThat(actualTrackings)
                .hasSize(1);
            assertThat(actualTrackings.getFirst().getChatId())
                .isEqualTo(5678L);
            assertThat(actualTrackings.getFirst().getLinkId())
                .isEqualTo(linkId2);
        }

        @Test
        public void deleteNonExistentTrackingTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 200L, "MeowMeowParrot", OffsetDateTime.now());
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
            var nonExistingTracking = new Tracking(200L, linkId);

            assertThatThrownBy(() -> trackingDao.delete(nonExistingTracking))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Tracking not found!");
        }
    }

    @Nested
    class FindAllTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM \"Tracking\"");
            jdbcTemplate.update("DELETE FROM \"Chat\"");
            jdbcTemplate.update("DELETE FROM \"Link\"");
        }

        @Test
        public void findAllEmptyTest() {
            var actualTrackings = trackingDao.findAll();

            assertThat(actualTrackings)
                .isEmpty();
        }

        @Test
        public void findAllOneTrackingTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1234L, "Scarlet", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://Scarlet-web.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://Scarlet-web.ru"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 1234L, linkId);

            var actualTrackings = trackingDao.findAll();

            assertThat(actualTrackings)
                .hasSize(1);
            assertThat(actualTrackings.getFirst().getChatId())
                .isEqualTo(1234L);
            assertThat(actualTrackings.getFirst().getLinkId())
                .isEqualTo(linkId);
        }

        @Test
        public void findAllSomeTrackingsTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 4455L, "Scarlet", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 5566L, "Scarlet", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 6677L, "Scarlet", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://uno.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://dos.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://tres.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId1 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://uno.com"
            );
            var linkId2 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://dos.com"
            );
            var linkId3 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://tres.com"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 4455L, linkId1);
            jdbcTemplate.update(trackingSql, 5566L, linkId2);
            jdbcTemplate.update(trackingSql, 6677L, linkId3);

            var actualTrackings = trackingDao.findAll();

            var actualTracking1 = actualTrackings.get(0);
            var actualTracking2 = actualTrackings.get(1);
            var actualTracking3 = actualTrackings.get(2);

            assertThat(actualTrackings)
                .hasSize(3);
            assertThat(actualTracking1.getChatId())
                .isEqualTo(4455L);
            assertThat(actualTracking1.getLinkId())
                .isEqualTo(linkId1);
            assertThat(actualTracking2.getChatId())
                .isEqualTo(5566L);
            assertThat(actualTracking2.getLinkId())
                .isEqualTo(linkId2);
            assertThat(actualTracking3.getChatId())
                .isEqualTo(6677L);
        }
    }

    @Nested
    class ExistsTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM \"Tracking\"");
            jdbcTemplate.update("DELETE FROM \"Chat\"");
            jdbcTemplate.update("DELETE FROM \"Link\"");
        }

        @Test
        public void existsTrackingFalseTest() {
            var tracking = new Tracking(1234L, 8L);

            var actualResult = trackingDao.exists(tracking);

            assertThat(actualResult)
                .isFalse();
        }

        @Test
        public void existsTrackingTrueTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 1234L, "Rabbit", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://jumpers.ru",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://jumpers.ru"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 1234L, linkId);
            var tracking = new Tracking(1234L, linkId);

            var actualResult = trackingDao.exists(tracking);

            assertThat(actualResult)
                .isTrue();
        }
    }

    @Nested
    class FindAllByChatIdTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM \"Tracking\"");
            jdbcTemplate.update("DELETE FROM \"Chat\"");
            jdbcTemplate.update("DELETE FROM \"Link\"");
        }

        @Test
        public void findAllByChatIdEmptyTest() {
            var actualTrackings = trackingDao.findAllByChatId(1234L);

            assertThat(actualTrackings)
                .isEmpty();
        }

        @Test
        public void findAllByChatIdSomeTrackingsTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 4455L, "Rabbit", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 5566L, "Hero", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://uno.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://dos.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://tres.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId1 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://uno.com"
            );
            var linkId2 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://dos.com"
            );
            var linkId3 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://tres.com"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 4455L, linkId1);
            jdbcTemplate.update(trackingSql, 4455L, linkId2);
            jdbcTemplate.update(trackingSql, 5566L, linkId3);

            var actualTrackings = trackingDao.findAllByChatId(4455L);

            assertThat(actualTrackings)
                .hasSize(2);
            assertThat(actualTrackings.getFirst().getChatId())
                .isEqualTo(4455L);
            assertThat(actualTrackings.getFirst().getLinkId())
                .isEqualTo(linkId1);
            assertThat(actualTrackings.getLast().getChatId())
                .isEqualTo(4455L);
            assertThat(actualTrackings.getLast().getLinkId())
                .isEqualTo(linkId2);
        }
    }

    @Nested
    class FindAllByLinkIdTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM \"Tracking\"");
            jdbcTemplate.update("DELETE FROM \"Chat\"");
            jdbcTemplate.update("DELETE FROM \"Link\"");
        }

        @Test
        public void findAllByLinkIdEmptyTest() {
            var actualTrackings = trackingDao.findAllByLinkId(1234L);

            assertThat(actualTrackings)
                .isEmpty();
        }

        @Test
        public void findAllByLinkIdSomeTrackingsTest() {
            String chatSql = "INSERT INTO \"Chat\" (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(chatSql, 4455L, "Rabbit", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 5566L, "Hero", OffsetDateTime.now());
            jdbcTemplate.update(chatSql, 6677L, "Villain", OffsetDateTime.now());
            String linkSql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(linkSql,
                "https://uno.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            jdbcTemplate.update(linkSql,
                "https://dos.com",
                OffsetDateTime.now(), MIN_DATE_TIME, OffsetDateTime.now()
            );
            var linkId1 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://uno.com"
            );
            var linkId2 = jdbcTemplate.queryForObject(
                "SELECT id FROM \"Link\" WHERE url = ?",
                Long.class,
                "https://dos.com"
            );
            String trackingSql = "INSERT INTO \"Tracking\" (chat_id, link_id) VALUES (?, ?)";
            jdbcTemplate.update(trackingSql, 4455L, linkId1);
            jdbcTemplate.update(trackingSql, 5566L, linkId2);
            jdbcTemplate.update(trackingSql, 6677L, linkId2);

            var actualTrackings = trackingDao.findAllByLinkId(linkId2);

            assertThat(actualTrackings)
                .hasSize(2);
            assertThat(actualTrackings.getFirst().getChatId())
                .isEqualTo(5566L);
            assertThat(actualTrackings.getFirst().getLinkId())
                .isEqualTo(linkId2);
            assertThat(actualTrackings.getLast().getChatId())
                .isEqualTo(6677L);
            assertThat(actualTrackings.getLast().getLinkId())
                .isEqualTo(linkId2);
        }
    }

}
