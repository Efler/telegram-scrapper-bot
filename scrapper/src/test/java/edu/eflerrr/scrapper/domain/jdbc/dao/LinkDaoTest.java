package edu.eflerrr.scrapper.domain.jdbc.dao;

import edu.eflerrr.scrapper.IntegrationTest;
import edu.eflerrr.scrapper.domain.jdbc.dto.Link;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = "classpath:scripts/clearLinkTable.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LinkDaoTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private LinkDao linkDao;
    private final OffsetDateTime staticDateTime = OffsetDateTime.of(
        2021, 1, 1,
        0, 0, 0,
        0, ZoneOffset.UTC
    );

    @Nested
    class AddTest {

        @Test
        public void addOneLinkTest() throws URISyntaxException {
            var testUrl = new URI("https://www.google.com");
            var link = new Link(testUrl, staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                linkDao.add(link);
            }
            var actualLinks = jdbcTemplate.query("SELECT * FROM \"Link\"", (rs, rowNum) ->
                {
                    try {
                        return new Link(
                            rs.getLong("id"),
                            new URI(rs.getString("url")),
                            rs.getObject("created_at", OffsetDateTime.class),
                            rs.getObject("checked_at", OffsetDateTime.class),
                            rs.getObject("updated_at", OffsetDateTime.class)
                        );
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            );

            assertThat(actualLinks)
                .hasSize(1);
            assertThat(actualLinks.getFirst().getUrl())
                .isEqualTo(testUrl);
            assertThat(actualLinks.getFirst().getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLinks.getFirst().getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLinks.getFirst().getUpdatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void addSomeLinksTest() throws URISyntaxException {
            var testUrl1 = new URI("https://www.first.com");
            var testUrl2 = new URI("https://www.second.com");
            var testUrl3 = new URI("https://www.third.com");
            var link1 = new Link(1L, testUrl1, staticDateTime);
            var link2 = new Link(2L, testUrl2, staticDateTime);
            var link3 = new Link(3L, testUrl3, staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                linkDao.add(link1);
                linkDao.add(link2);
                linkDao.add(link3);
            }
            var actualLinks = jdbcTemplate.query("SELECT * FROM \"Link\"", (rs, rowNum) ->
                {
                    try {
                        return new Link(
                            rs.getLong("id"),
                            new URI(rs.getString("url")),
                            rs.getObject("created_at", OffsetDateTime.class),
                            rs.getObject("checked_at", OffsetDateTime.class),
                            rs.getObject("updated_at", OffsetDateTime.class)
                        );
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            );
            var actualLink1 = actualLinks.get(0);
            var actualLink2 = actualLinks.get(1);
            var actualLink3 = actualLinks.get(2);

            assertThat(actualLinks)
                .hasSize(3);

            assertThat(actualLink1.getUrl())
                .isEqualTo(testUrl1);
            assertThat(actualLink1.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink1.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink1.getUpdatedAt())
                .isEqualTo(staticDateTime);

            assertThat(actualLink2.getUrl())
                .isEqualTo(testUrl2);
            assertThat(actualLink2.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink2.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink1.getUpdatedAt())
                .isEqualTo(staticDateTime);

            assertThat(actualLink3.getUrl())
                .isEqualTo(testUrl3);
            assertThat(actualLink3.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink3.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink1.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void addLinkWithNullUrlTest() {
            var invalidLink = new Link(null, staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                assertThatThrownBy(() -> linkDao.add(invalidLink))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Link or URL is null!");
            }
        }

        @Test
        public void addSameLinkTest() throws URISyntaxException {
            var url = new URI("https://www.first.com");
            var link1 = new Link(url, staticDateTime);
            var link2 = new Link(url, staticDateTime);
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                linkDao.add(link1);
                assertThatThrownBy(() -> linkDao.add(link2))
                    .isInstanceOf(DataIntegrityViolationException.class);
            }
        }
    }

    @Nested
    class DeleteTest {

        @Test
        public void deleteOneLinkTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://hey.ru", staticDateTime, MIN_DATE_TIME, staticDateTime);
            var link = new Link(new URI("https://hey.ru"), staticDateTime);

            linkDao.delete(link);
            var actualLinks = jdbcTemplate.query("SELECT * FROM \"Link\"", (rs, rowNum) ->
                {
                    try {
                        return new Link(
                            rs.getLong("id"),
                            new URI(rs.getString("url")),
                            rs.getObject("created_at", OffsetDateTime.class),
                            rs.getObject("checked_at", OffsetDateTime.class),
                            rs.getObject("updated_at", OffsetDateTime.class)
                        );
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            );

            assertThat(actualLinks)
                .isEmpty();
        }

        @Test
        public void deleteSomeLinksTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://hey1.ru", staticDateTime, MIN_DATE_TIME, staticDateTime);
            jdbcTemplate.update(sql, "https://hey2.ru", staticDateTime, MIN_DATE_TIME, staticDateTime);
            jdbcTemplate.update(sql, "https://hey3.ru", staticDateTime, MIN_DATE_TIME, staticDateTime);
            var link1 = new Link(new URI("https://hey1.ru"), staticDateTime);
            var link2 = new Link(new URI("https://hey2.ru"), staticDateTime);
            var link3 = new Link(new URI("https://hey3.ru"), staticDateTime);

            linkDao.delete(link1);
            linkDao.delete(link3);
            var actualLinks = jdbcTemplate.query("SELECT * FROM \"Link\"", (rs, rowNum) ->
                {
                    try {
                        return new Link(
                            rs.getLong("id"),
                            new URI(rs.getString("url")),
                            rs.getObject("created_at", OffsetDateTime.class),
                            rs.getObject("checked_at", OffsetDateTime.class),
                            rs.getObject("updated_at", OffsetDateTime.class)
                        );
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            );

            assertThat(actualLinks)
                .hasSize(1);
            assertThat(link2.getUrl())
                .isEqualTo(new URI("https://hey2.ru"));
            assertThat(link2.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(link2.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(link2.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void deleteNonExistentLinkTest() throws URISyntaxException {
            var nonExistentLink = new Link(new URI("https://yolo.ua"), staticDateTime);

            assertThatThrownBy(() -> linkDao.delete(nonExistentLink))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Link not found!");
        }
    }

    @Nested
    class FindAllTest {

        @Test
        public void findAllEmptyTest() {
            var actualLinks = linkDao.findAll();

            assertThat(actualLinks)
                .isEmpty();
        }

        @Test
        public void findAllOneLinkTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://wowowow.ru", staticDateTime, MIN_DATE_TIME, staticDateTime);

            var actualLinks = linkDao.findAll();

            assertThat(actualLinks)
                .hasSize(1);
            assertThat(actualLinks.getFirst().getUrl())
                .isEqualTo(new URI("https://wowowow.ru"));
            assertThat(actualLinks.getFirst().getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLinks.getFirst().getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLinks.getFirst().getUpdatedAt())
                .isEqualTo(staticDateTime);
        }

        @Test
        public void findAllSomeLinksTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://rabbit.com", staticDateTime, MIN_DATE_TIME, staticDateTime);
            jdbcTemplate.update(sql, "https://wolf.com", staticDateTime, MIN_DATE_TIME, staticDateTime);
            jdbcTemplate.update(sql, "https://fox.com", staticDateTime, MIN_DATE_TIME, staticDateTime);

            var actualLinks = linkDao.findAll();
            var actualLink1 = actualLinks.get(0);
            var actualLink2 = actualLinks.get(1);
            var actualLink3 = actualLinks.get(2);

            assertThat(actualLinks)
                .hasSize(3);

            assertThat(actualLink1.getUrl())
                .isEqualTo(new URI("https://rabbit.com"));
            assertThat(actualLink1.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink1.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink1.getUpdatedAt())
                .isEqualTo(staticDateTime);

            assertThat(actualLink2.getUrl())
                .isEqualTo(new URI("https://wolf.com"));
            assertThat(actualLink2.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink2.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink1.getUpdatedAt())
                .isEqualTo(staticDateTime);

            assertThat(actualLink3.getUrl())
                .isEqualTo(new URI("https://fox.com"));
            assertThat(actualLink3.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink3.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink1.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }
    }

    @Nested
    class ExistsTest {

        @Test
        public void existsLinkFalseTest() throws URISyntaxException {
            var link = new Link(new URI("https://this-is-the-way.com"), staticDateTime);

            var actualResult = linkDao.exists(link);

            assertThat(actualResult)
                .isFalse();
        }

        @Test
        public void existsLinkTrueTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://crow.ru", staticDateTime, MIN_DATE_TIME, staticDateTime);
            var link = new Link(new URI("https://crow.ru"), staticDateTime);

            var actualResult = linkDao.exists(link);

            assertThat(actualResult)
                .isTrue();
        }
    }

    @Nested
    class FindAllWithFilterTest {

        @Test
        public void findAllWithFilterEmptyTest() {
            try (MockedStatic<OffsetDateTime> theMock = Mockito.mockStatic(OffsetDateTime.class)) {
                theMock.when(OffsetDateTime::now)
                    .thenReturn(staticDateTime);

                var actualLinks = linkDao.findAllWithFilter(Duration.ofSeconds(15), staticDateTime);

                assertThat(actualLinks)
                    .isEmpty();
            }
        }

        @Test
        public void findAllWithFilterSomeLinksTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://rabbit.com", staticDateTime,
                staticDateTime.minusSeconds(30), staticDateTime
            );
            jdbcTemplate.update(sql, "https://wolf.com", staticDateTime,
                staticDateTime.minusSeconds(4), staticDateTime
            );
            jdbcTemplate.update(sql, "https://fox.com", staticDateTime,
                staticDateTime.minusSeconds(16), staticDateTime
            );

            var filteredLinks = linkDao.findAllWithFilter(Duration.ofSeconds(15), staticDateTime);

            assertThat(filteredLinks)
                .hasSize(2);

            assertThat(filteredLinks.getFirst().getUrl())
                .isEqualTo(new URI("https://rabbit.com"));
            assertThat(filteredLinks.getFirst().getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(filteredLinks.getFirst().getCheckedAt())
                .isEqualTo(staticDateTime.minusSeconds(30));
            assertThat(filteredLinks.getFirst().getUpdatedAt())
                .isEqualTo(staticDateTime);

            assertThat(filteredLinks.getLast().getUrl())
                .isEqualTo(new URI("https://fox.com"));
            assertThat(filteredLinks.getLast().getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(filteredLinks.getLast().getCheckedAt())
                .isEqualTo(staticDateTime.minusSeconds(16));
            assertThat(filteredLinks.getLast().getUpdatedAt())
                .isEqualTo(staticDateTime);
        }
    }

    @Nested
    class getLinkByIdTest {

        @Test
        public void getLinkByIdEmptyTest() {
            assertThatThrownBy(() -> linkDao.getLinkById(1L))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Link not found!");
        }

        @Test
        public void getLinkByIdSuccessfulTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://i-exist.ua", staticDateTime, MIN_DATE_TIME, staticDateTime);
            sql = "SELECT id FROM \"Link\" WHERE url = ?";
            var linkId = jdbcTemplate.queryForObject(sql, Long.class, "https://i-exist.ua");

            assertThat(linkId)
                .isNotNull();
            var actualLink = linkDao.getLinkById(linkId);
            assertThat(actualLink)
                .isNotNull();
            assertThat(actualLink.getId())
                .isEqualTo(linkId);
            assertThat(actualLink.getUrl())
                .isEqualTo(new URI("https://i-exist.ua"));
            assertThat(actualLink.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }
    }

    @Nested
    class getIdTest {

        @Test
        public void getIdEmptyTest() throws URISyntaxException {
            var link = new Link(new URI("https://i-dont-exist.ua"), staticDateTime);

            assertThatThrownBy(() -> linkDao.getId(link))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Link not found!");
        }

        @Test
        public void getIdNullLinkTest() {
            assertThatThrownBy(() -> linkDao.getId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Link or URL is null!");
        }

        @Test
        public void getIdSuccessfulTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, "https://i-exist.ua", staticDateTime, MIN_DATE_TIME, staticDateTime);
            var link = new Link(new URI("https://i-exist.ua"), staticDateTime);
            sql = "SELECT id FROM \"Link\" WHERE url = ?";
            var expectedId = jdbcTemplate.queryForObject(sql, Long.class, "https://i-exist.ua");

            var actualId = linkDao.getId(link);

            assertThat(actualId)
                .isNotNull();
            assertThat(actualId)
                .isEqualTo(expectedId);
        }
    }

    @Nested
    class UpdateCheckedAtTest {

        @Test
        public void updateCheckedAtEmptyTest() throws URISyntaxException {
            var link = new Link(1L, new URI("https://i-dont-exist.ua"), staticDateTime);

            assertThatThrownBy(() -> linkDao.updateCheckedAt(link, staticDateTime))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Link not found!");
        }

        @Test
        public void updateCheckedAtNullLinkTest() {
            assertThatThrownBy(() -> linkDao.updateCheckedAt(null, staticDateTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Link or URL is null!");
        }

        @Test
        public void updateCheckedAtSuccessfulTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                "https://i-exist.ua", staticDateTime, OffsetDateTime.MAX, staticDateTime
            );
            var link = new Link(
                1L, new URI("https://i-exist.ua"), staticDateTime, OffsetDateTime.MAX, staticDateTime
            );

            linkDao.updateCheckedAt(link, staticDateTime);

            var actualLink =
                jdbcTemplate.queryForObject("SELECT * FROM \"Link\" WHERE url = 'https://i-exist.ua'", (rs, rowNum) ->
                    {
                        try {
                            return new Link(
                                rs.getLong("id"),
                                new URI(rs.getString("url")),
                                rs.getObject("created_at", OffsetDateTime.class),
                                rs.getObject("checked_at", OffsetDateTime.class),
                                rs.getObject("updated_at", OffsetDateTime.class)
                            );
                        } catch (URISyntaxException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                );

            assertThat(actualLink)
                .isNotNull();
            assertThat(actualLink.getUrl())
                .isEqualTo(new URI("https://i-exist.ua"));
            assertThat(actualLink.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink.getCheckedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }
    }

    @Nested
    class UpdateUpdatedAtTest {

        @Test
        public void updateUpdatedAtEmptyTest() throws URISyntaxException {
            var link = new Link(1L, new URI("https://i-dont-exist.ua"), staticDateTime);

            assertThatThrownBy(() -> linkDao.updateUpdatedAt(link, staticDateTime))
                .isInstanceOf(InvalidDataAccessResourceUsageException.class)
                .hasMessage("Link not found!");
        }

        @Test
        public void updateUpdatedAtNullLinkTest() {
            assertThatThrownBy(() -> linkDao.updateUpdatedAt(null, staticDateTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Link or URL is null!");
        }

        @Test
        public void updateUpdatedAtSuccessfulTest() throws URISyntaxException {
            String sql = "INSERT INTO \"Link\" (url, created_at, checked_at, updated_at) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                "https://i-exist.ua", staticDateTime, MIN_DATE_TIME, OffsetDateTime.MAX
            );
            var link = new Link(
                1L, new URI("https://i-exist.ua"), staticDateTime, MIN_DATE_TIME, OffsetDateTime.MAX
            );

            linkDao.updateUpdatedAt(link, staticDateTime);

            var actualLink = jdbcTemplate.queryForObject(
                "SELECT * FROM \"Link\" WHERE url = 'https://i-exist.ua'",
                (rs, rowNum) ->
                {
                    try {
                        return new Link(
                            rs.getLong("id"),
                            new URI(rs.getString("url")),
                            rs.getObject("created_at", OffsetDateTime.class),
                            rs.getObject("checked_at", OffsetDateTime.class),
                            rs.getObject("updated_at", OffsetDateTime.class)
                        );
                    } catch (URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            );

            assertThat(actualLink)
                .isNotNull();
            assertThat(actualLink.getId())
                .isNotNull();
            assertThat(actualLink.getUrl())
                .isEqualTo(new URI("https://i-exist.ua"));
            assertThat(actualLink.getCreatedAt())
                .isEqualTo(staticDateTime);
            assertThat(actualLink.getCheckedAt())
                .isEqualTo(MIN_DATE_TIME);
            assertThat(actualLink.getUpdatedAt())
                .isEqualTo(staticDateTime);
        }
    }

}
