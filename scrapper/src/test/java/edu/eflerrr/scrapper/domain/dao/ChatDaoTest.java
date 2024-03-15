package edu.eflerrr.scrapper.domain.dao;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.dto.Chat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
class ChatDaoTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ChatDao chatDao;
    private final OffsetDateTime staticDateTime = OffsetDateTime.of(
        2021, 1, 1,
        0, 0, 0,
        0, ZoneOffset.UTC
    );

    @Nested
    class AddTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Chat");
        }

        @Test
        public void addOneChatTest() {
            var chat = new Chat(4444L, "TestUsername", staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                chatDao.add(chat);
            }
            var actualChats = jdbcTemplate.query("SELECT * FROM Chat", (rs, rowNum) ->
                new Chat(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getObject("created_at", OffsetDateTime.class)
                )
            );

            assertThat(actualChats)
                .hasSize(1);
            assertThat(actualChats.getFirst().getId())
                .isEqualTo(4444L);
            assertThat(actualChats.getFirst().getUsername())
                .isEqualTo("TestUsername");
            assertThat(actualChats.getFirst().getCreatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void addSomeChatsTest() {
            var chat1 = new Chat(4444L, "1-kitty", staticDateTime);
            var chat2 = new Chat(5555L, "kitty-2", staticDateTime);
            var chat3 = new Chat(6677555L, "3-kitty-3", staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                chatDao.add(chat1);
                chatDao.add(chat2);
                chatDao.add(chat3);
            }
            var actualChats = jdbcTemplate.query("SELECT * FROM Chat", (rs, rowNum) ->
                new Chat(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getObject("created_at", OffsetDateTime.class)
                )
            );
            var actualChat1 = actualChats.get(0);
            var actualChat2 = actualChats.get(1);
            var actualChat3 = actualChats.get(2);

            assertThat(actualChats)
                .hasSize(3);
            assertThat(actualChat1)
                .isEqualTo(chat1);
            assertThat(actualChat2)
                .isEqualTo(chat2);
            assertThat(actualChat3)
                .isEqualTo(chat3);
        }

        @Test
        public void addChatWithNullIdTest() {
            var invalidChat = new Chat(null, "WrongGuy1337", staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                assertThatThrownBy(() -> chatDao.add(invalidChat))
                    .isInstanceOf(DataIntegrityViolationException.class);
            }
        }
    }

    @Nested
    class DeleteTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Chat");
        }

        @Test
        public void deleteOneChatTest() {
            String sql = "INSERT INTO Chat (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 1234, "MillyPops", staticDateTime);
            var chat = new Chat(1234L, "MillyPops", staticDateTime);

            chatDao.delete(chat);
            var actualChats = jdbcTemplate.query("SELECT * FROM Chat", (rs, rowNum) ->
                new Chat(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getObject("created_at", OffsetDateTime.class)
                )
            );

            assertThat(actualChats)
                .isEmpty();
        }

        @Test
        public void deleteSomeChatsTest() {
            String sql = "INSERT INTO Chat (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 1234, "MillyPops", staticDateTime);
            jdbcTemplate.update(sql, 5678, "HeyHeyDoggy", staticDateTime);
            jdbcTemplate.update(sql, 9999, "KittyKitty", staticDateTime);
            var chat1 = new Chat(1234L, "MillyPops", staticDateTime);
            var chat2 = new Chat(5678L, "HeyHeyDoggy", staticDateTime);
            var chat3 = new Chat(9999L, "KittyKitty", staticDateTime);

            chatDao.delete(chat1);
            chatDao.delete(chat3);
            var actualChats = jdbcTemplate.query("SELECT * FROM Chat", (rs, rowNum) ->
                new Chat(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getObject("created_at", OffsetDateTime.class)
                )
            );

            assertThat(actualChats)
                .hasSize(1)
                .containsExactly(chat2);
        }

        @Test
        public void deleteNonExistentChatTest() {
            var nonExistingChat = new Chat(5555L, "HeyHeyDoggy", staticDateTime);

            assertThatThrownBy(() -> chatDao.delete(nonExistingChat))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Chat not found!");
        }
    }

    @Nested
    class FindAllTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Chat");
        }

        @Test
        public void findAllEmptyTest() {
            var actualChats = chatDao.findAll();

            assertThat(actualChats)
                .isEmpty();
        }

        @Test
        public void findAllOneChatTest() {
            String sql = "INSERT INTO Chat (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 8888, "MeowMeowParrot", staticDateTime);
            var expectedChat = new Chat(8888L, "MeowMeowParrot", staticDateTime);

            var actualChats = chatDao.findAll();

            assertThat(actualChats)
                .hasSize(1)
                .containsExactly(expectedChat);
        }

        @Test
        public void findAllSomeChatsTest() {
            String sql = "INSERT INTO Chat (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 1000, "OneSheep", staticDateTime);
            jdbcTemplate.update(sql, 2000, "TwoSheep", staticDateTime);
            jdbcTemplate.update(sql, 3000, "ThreeSheep", staticDateTime);
            var expectedChat1 = new Chat(1000L, "OneSheep", staticDateTime);
            var expectedChat2 = new Chat(2000L, "TwoSheep", staticDateTime);
            var expectedChat3 = new Chat(3000L, "ThreeSheep", staticDateTime);

            var actualChats = chatDao.findAll();

            assertThat(actualChats)
                .hasSize(3)
                .containsExactly(expectedChat1, expectedChat2, expectedChat3);
        }
    }

    @Nested
    class ExistsTest {

        @AfterEach
        public void cleanUp() {
            jdbcTemplate.update("DELETE FROM Chat");
        }

        @Test
        public void existsChatFalseTest() {
            var chat = new Chat(1234L, "MillyPops", staticDateTime);

            var actualResult = chatDao.exists(chat);

            assertThat(actualResult)
                .isFalse();
        }

        @Test
        public void existsChatTrueTest() {
            String sql = "INSERT INTO Chat (id, username, created_at) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, 1234, "MillyPops", staticDateTime);
            var chat = new Chat(1234L, "MillyPops", staticDateTime);

            var actualResult = chatDao.exists(chat);

            assertThat(actualResult)
                .isTrue();
        }
    }

}
