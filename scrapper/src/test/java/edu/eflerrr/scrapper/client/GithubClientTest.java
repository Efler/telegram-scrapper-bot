package edu.eflerrr.scrapper.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.eflerrr.scrapper.ScrapperApplication;
import edu.eflerrr.scrapper.client.dto.response.GithubClientResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ScrapperApplication.class)
@WireMockTest
class GithubClientTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort().dynamicPort())
        .build();
    @Autowired
    private GithubClient githubClient;

    @DynamicPropertySource
    public static void mockGithubBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("app.api.github-base-url", wireMock::baseUrl);
    }

    @Test
    public void successfulFetchResponse() {
        String jsonResponseBody = """
            {
              "id": 1.35035186E8,
              "node_id": "MDEwOlJlcG9zaXRkxMzUwMzUxODY\\u003d",
              "name": "Doggy",
              "full_name": "GoodBoy/Doggy",
              "private": false,
              "owner": {
              },
              "html_url": "https://github.com/GoodBoy/Doggy",
              "description": "My favourite pet!",
              "fork": false,
              "url": "https://api.github.com/repos/GoodBoy/Doggy",
              "created_at": "2018-05-27T10:09:13Z",
              "updated_at": "2024-02-20T07:02:48Z",
              "pushed_at": "2023-10-30T13:49:14Z",
              "git_url": "git://github.com/GoodBoy/Doggy.git",
              "ssh_url": "git@github.com:GoodBoy/Doggy.git",
              "clone_url": "https://github.com/GoodBoy/Doggy.git",
              "svn_url": "https://github.com/GoodBoy/Doggy",
              "homepage": "",
              "size": 372.0,
              "stargazers_count": 1482.0,
              "watchers_count": 1482.0,
              "language": "Python",
              "has_issues": true,
              "has_projects": false,
              "has_downloads": true,
              "has_wiki": true,
              "has_pages": false,
              "has_discussions": false,
              "visibility": "public",
              "forks": 337.0,
              "open_issues": 100.0,
              "watchers": 1482.0,
              "default_branch": "master",
              "network_count": 337.0,
              "subscribers_count": 68.0
            }""";
        wireMock.stubFor(get(urlEqualTo("/repos/GoodBoy/Doggy"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jsonResponseBody)));

        var actualResponse = githubClient.fetchResponse("/GoodBoy/Doggy");

        var expectedResponse = new GithubClientResponse(
            135_035_186L,
            "Doggy",
            OffsetDateTime.parse("2024-02-20T07:02:48Z"),
            OffsetDateTime.parse("2023-10-30T13:49:14Z")
        );
        assertThat(actualResponse)
            .isEqualTo(expectedResponse);
    }

    @Test
    public void clientErrorFetchResponse() {
        wireMock.stubFor(get(urlEqualTo("/repos/candies/sweets"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\": \"Not Found\"}")));

        assertThatThrownBy(() -> githubClient.fetchResponse("/candies/sweets"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Client error during fetchResponse in GithubClient! Message: 404 Not Found"
            );
    }

    @Test
    public void serverErrorFetchResponse() {
        wireMock.stubFor(get(urlEqualTo("/repos/cookies/donuts"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\": \"Not today, sir!\"}")));

        assertThatThrownBy(() -> githubClient.fetchResponse("/cookies/donuts"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Server error during fetchResponse in GithubClient! Message: 503 Service Unavailable"
            );
    }

    @Test
    public void unexpectedBehaviorFetchResponse() {
        wireMock.stubFor(get(urlEqualTo("/repos/apples/pears"))
            .willReturn(aResponse()
                .withStatus(301)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\": \"Hello!\"}")));

        assertThatThrownBy(() -> githubClient.fetchResponse("/apples/pears"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Unexpected status code during fetchResponse in GithubClient! Message: 301 Moved Permanently"
            );
    }

    @Test
    public void invalidDataFetchResponse() {
        String jsonResponseBody = """
            {
              "forks": 337.0,
              "open_issues": 100.0,
              "watchers": 1482.0,
              "default_branch": "master",
              "network_count": 337.0,
              "subscribers_count": 68.0,
              "pushed_at": "2023-10-30T13:49:14Z"
            }""";
        wireMock.stubFor(get(urlEqualTo("/repos/Invalid/Data"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jsonResponseBody)));

        assertThatThrownBy(() -> githubClient.fetchResponse("/Invalid/Data"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Error occurred during fetchResponse in GithubClient! Message: empty response/data"
            );
    }

}
