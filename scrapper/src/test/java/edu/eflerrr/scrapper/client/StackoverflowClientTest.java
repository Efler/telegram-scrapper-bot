package edu.eflerrr.scrapper.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import edu.eflerrr.scrapper.ScrapperApplication;
import edu.eflerrr.scrapper.client.dto.response.StackoverflowClientResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext
class StackoverflowClientTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().dynamicPort().dynamicPort())
        .build();
    @Autowired
    private StackoverflowClient stackoverflowClient;

    @DynamicPropertySource
    public static void mockStackoverflowBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("app.api.stackoverflow-base-url", wireMock::baseUrl);
    }

    @Test
    public void successfulFetchResponse() {
        String jsonResponseBody = """
            {
              "items": [
                {
                  "owner": {
                    "account_id": 4661860.0,
                    "reputation": 33.0,
                    "user_id": 3842685.0,
                    "user_type": "registered",
                    "profile_image": "https://smth.com",
                    "display_name": "Igonaf",
                    "link": "https://smthagain.com"
                  },
                  "creation_date": 1.708697431E9,
                  "post_id": 7.8047964E7,
                  "question_id": 7.8047875E7,
                  "timeline_type": "answer"
                },
                {
                  "owner": {
                    "account_id": 4661860.0,
                    "reputation": 33.0,
                    "user_id": 3842685.0,
                    "user_type": "registered",
                    "profile_image": "https://somepic.com",
                    "display_name": "Igonaf",
                    "link": "https://somelinke.com"
                  },
                  "creation_date": 1.708696499E9,
                  "question_id": 7.8047875E7,
                  "timeline_type": "question"
                }
              ],
              "has_more": false,
              "quota_max": 300.0,
              "quota_remaining": 299.0
            }""";
        wireMock.stubFor(get(urlEqualTo(
            "/questions/777777/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jsonResponseBody)));

        var actualResponse = stackoverflowClient.fetchResponse(777777L);

        StackoverflowClientResponse expectedResponse = new StackoverflowClientResponse(
            OffsetDateTime.parse("2024-02-23T14:10:31Z"),
            new ArrayList<>(List.of(
                new StackoverflowClientResponse.Event(
                    OffsetDateTime.parse("2024-02-23T14:10:31Z"), "answer"
                ),
                new StackoverflowClientResponse.Event(
                    OffsetDateTime.parse("2024-02-23T13:54:59Z"), "question"
                )
            ))
        );
        assertThat(actualResponse)
            .isEqualTo(expectedResponse);
    }

    @Test
    public void clientErrorFetchResponse() {
        wireMock.stubFor(get(urlEqualTo(
            "/questions/4435432/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\": \"Not Found\"}")));

        assertThatThrownBy(() -> stackoverflowClient.fetchResponse(4435432L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Client error during fetchResponse in StackoverflowClient! Message: 404 Not Found"
            );
    }

    @Test
    public void serverErrorFetchResponse() {
        wireMock.stubFor(get(urlEqualTo(
            "/questions/111111/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\": \"Im feeling not good\"}")));

        assertThatThrownBy(() -> stackoverflowClient.fetchResponse(111111L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Server error during fetchResponse in StackoverflowClient! Message: 502 Bad Gateway"
            );
    }

    @Test
    public void unexpectedBehaviorFetchResponse() {
        wireMock.stubFor(get(urlEqualTo(
            "/questions/222222/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(305)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\": \"Welcome!\"}")));

        assertThatThrownBy(() -> stackoverflowClient.fetchResponse(222222L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Unexpected status code during fetchResponse in StackoverflowClient! Message: 305 Use Proxy"
            );
    }

    @Test
    public void noItemsFetchResponse() {
        String jsonResponseBody = """
            {
              "has_more": false,
              "quota_max": 300.0,
              "quota_remaining": 299.0
            }""";
        wireMock.stubFor(get(urlEqualTo(
            "/questions/5555222/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jsonResponseBody)));

        assertThatThrownBy(() -> stackoverflowClient.fetchResponse(5555222L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Error occurred during fetchResponse in StackoverflowClient! Message: empty response/data"
            );
    }

    @Test
    public void emptyItemsFetchResponse() {
        String jsonResponseBody = """
            {
              "items": [],
              "has_more": false,
              "quota_max": 300.0,
              "quota_remaining": 299.0
            }""";
        wireMock.stubFor(get(urlEqualTo(
            "/questions/333333/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jsonResponseBody)));

        assertThatThrownBy(() -> stackoverflowClient.fetchResponse(333333L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Error occurred during fetchResponse in StackoverflowClient! Message: empty response/data"
            );
    }

    @Test
    public void emptyDataFetchResponse() {
        String jsonResponseBody = """
            {
              "items": [
                {
                  "owner": {
                    "account_id": 4661860.0,
                    "reputation": 33.0,
                    "user_id": 3842685.0,
                    "user_type": "registered",
                    "profile_image": "https://smth.com",
                    "display_name": "Igonaf",
                    "link": "https://smthagain.com"
                  },
                  "creation_date": 1.708697431E9,
                  "post_id": 7.8047964E7,
                  "question_id": 7.8047875E7,
                  "timeline_type": "answer"
                },
                {
                  "owner": {
                    "account_id": 4661860.0,
                    "reputation": 33.0,
                    "user_id": 3842685.0,
                    "user_type": "registered",
                    "profile_image": "https://somepic.com",
                    "display_name": "Igonaf",
                    "link": "https://somelinke.com"
                  },
                  "question_id": 7.8047875E7,
                  "timeline_type": "question"
                }
              ],
              "has_more": false,
              "quota_max": 300.0,
              "quota_remaining": 299.0
            }""";
        wireMock.stubFor(get(urlEqualTo(
            "/questions/1098098/timeline?order=desc&sort=creation_date&site=stackoverflow")
        )
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jsonResponseBody)));

        assertThatThrownBy(() -> stackoverflowClient.fetchResponse(1098098L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(
                "Error occurred during fetchResponse in StackoverflowClient! Message: empty response/data"
            );
    }

}
