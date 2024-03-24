package edu.eflerrr.scrapper.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.eflerrr.scrapper.ScrapperApplication;
import edu.eflerrr.scrapper.client.dto.response.GithubBranchResponse;
import edu.eflerrr.scrapper.client.dto.response.GithubClientResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ScrapperApplication.class)
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=none"})
@WireMockTest
@DirtiesContext
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

    private static final String TEST_REPO_RESPONSE_BODY = """
        {
          "id": 1.35035186E8,
          "node_id": "MDEwOlJlcG9zaXRkxMzUwMzUxODYu003d",
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
        }
        """;
    private static final String TEST_BRANCH_RESPONSE_BODY = """
        [
          {
            "name": "branch-1",
            "commit": {
              "sha": "c5b97d5ae6c19d5c5df71a34c7fbeeda2479ccbc",
              "url": "https://api.github.com/repos/octocat/Hello-World/commits/c5b97d5ae6c19d5c5df71a34c7fbeeda2479ccbc"
            },
            "protected": true,
            "protection": {
              "required_status_checks": {
                "enforcement_level": "non_admins",
                "contexts": [
                  "ci-test",
                  "linter"
                ]
              }
            },
            "protection_url": "https://api.github.com/repos/octocat/hello-world/branches/master/protection"
          },
          {
            "name": "branch-2",
            "commit": {
              "sha": "c5b97d5ae346456456457457345beeda2479ccbc",
              "url": "https://api.github.com/repos/octocat/Hello-World/commits/c5b97d5ae346456456457457345beeda2479ccbc"
            },
            "protected": true,
            "protection": {
              "required_status_checks": {
                "enforcement_level": "non_admins",
                "contexts": [
                  "ci-test",
                  "linter"
                ]
              }
            },
            "protection_url": "https://api.github.com/repos/octocat/hello-world/branches/master/protection"
          }
        ]
        """;
    private static final String TEST_FIRST_BRANCH_RESPONSE_BODY = """
        {
          "name": "branch-1",
          "commit": {
            "sha": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
            "node_id": "MDY6Q29tbWl0MTI5NjI2OTo3ZmQxYTYwYjAxZjkxYjMxNGY1OTk1NWE0ZTRkNGU4MGQ4ZWRmMTFk",
            "commit": {
              "author": {
                "name": "The Octocat",
                "email": "octocat@nowhere.com",
                "date": "2012-03-06T23:06:50Z"
              },
              "committer": {
                "name": "The Octocat",
                "email": "octocat@nowhere.com",
                "date": "2012-03-06T23:06:50Z"
              },
              "message": "Merge pull request #6 from Spaceghost/patch-1\\n\\nNew line at end of file.",
              "tree": {
                "sha": "b4eecafa9be2f2006ce1b709d6857b07069b4608",
                "url": "https://api.github.com/repos/octocat/Hello-World/git/trees/b4eecafa9be2f2006ce1b709d6857b07069b4608"
              },
              "url": "https://api.github.com/repos/octocat/Hello-World/git/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
              "comment_count": 77,
              "verification": {
                "verified": false,
                "reason": "unsigned",
                "signature": null,
                "payload": null
              }
            }
          }
        }
        """;
    private static final String TEST_SECOND_BRANCH_RESPONSE_BODY = """
        {
          "name": "branch-2",
          "commit": {
            "sha": "45655555555555s5sd55sfs5d4f6s5d4f654sd65f4s5df",
            "node_id": "MDY6Q29tbWl0MTI5NjI2OTo3ZmQxYTYwYjAxZjkxYjMxNGY1OTk1NWE0ZTRkNGU4MGQ4ZWRmMTFk",
            "commit": {
              "author": {
                "name": "The Octocat",
                "email": "octocat@nowhere.com",
                "date": "2012-03-06T23:06:50Z"
              },
              "committer": {
                "name": "The Octocat",
                "email": "octocat@nowhere.com",
                "date": "2023-10-07T10:05:40Z"
              },
              "message": "Merge pull request #6 from Spaceghost/patch-1\\n\\nNew line at end of file.",
              "tree": {
                "sha": "b4eecafa9be2f2006ce1b709d6857b07069b4608",
                "url": "https://api.github.com/repos/octocat/Hello-World/git/trees/b4eecafa9be2f2006ce1b709d6857b07069b4608"
              },
              "url": "https://api.github.com/repos/octocat/Hello-World/git/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
              "comment_count": 77,
              "verification": {
                "verified": false,
                "reason": "unsigned",
                "signature": null,
                "payload": null
              }
            }
          }
        }
        """;

    private GithubClientResponse createExpectedSuccessfulResponse() {
        var expectedBranches = new ArrayList<GithubBranchResponse>();
        expectedBranches.add(new GithubBranchResponse(
            "branch-1",
            OffsetDateTime.parse("2012-03-06T23:06:50Z")
        ));
        expectedBranches.add(new GithubBranchResponse(
            "branch-2",
            OffsetDateTime.parse("2023-10-07T10:05:40Z")
        ));
        return new GithubClientResponse(
            135_035_186L,
            "Doggy",
            OffsetDateTime.parse("2024-02-20T07:02:48Z"),
            OffsetDateTime.parse("2023-10-30T13:49:14Z"),
            expectedBranches
        );
    }

    @Test
    public void successfulFetchResponse() {
        wireMock.stubFor(get(urlEqualTo("/repos/GoodBoy/Doggy"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(TEST_REPO_RESPONSE_BODY)));
        wireMock.stubFor(get(urlEqualTo("/repos/GoodBoy/Doggy/branches"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(TEST_BRANCH_RESPONSE_BODY)));
        wireMock.stubFor(get(urlEqualTo("/repos/GoodBoy/Doggy/branches/branch-1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(TEST_FIRST_BRANCH_RESPONSE_BODY)));
        wireMock.stubFor(get(urlEqualTo("/repos/GoodBoy/Doggy/branches/branch-2"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(TEST_SECOND_BRANCH_RESPONSE_BODY)));

        var actualResponse = githubClient.fetchResponse("GoodBoy", "Doggy");

        var expectedResponse = createExpectedSuccessfulResponse();
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

        assertThatThrownBy(() -> githubClient.fetchResponse(
            "candies", "sweets")
        )
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

        assertThatThrownBy(() -> githubClient.fetchResponse(
            "cookies", "donuts")
        )
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

        assertThatThrownBy(() -> githubClient.fetchResponse(
            "apples", "pears")
        )
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

        assertThatThrownBy(() -> githubClient.fetchResponse(
            "Invalid", "Data")
        )
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Error occurred during fetchResponse in GithubClient! Message: empty response/data"
            );
    }

}
