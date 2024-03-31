package edu.eflerrr.bot.client;

import edu.eflerrr.bot.client.dto.request.LinkRequest;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.client.dto.response.ListLinksResponse;
import edu.eflerrr.bot.client.dto.response.ScrapperErrorResponse;
import edu.eflerrr.bot.exception.DuplicateLinkPostException;
import edu.eflerrr.bot.exception.DuplicateRegistrationException;
import edu.eflerrr.bot.exception.InvalidDataException;
import edu.eflerrr.bot.exception.LinkNotFoundException;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import edu.eflerrr.bot.exception.retry.RetryableRequestException;
import java.net.URI;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@SuppressWarnings("MemberName")
public class ScrapperClient {
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";
    private static final String TG_CHAT_ENDPOINT = "/tg-chat";
    private static final String LINKS_ENDPOINT = "/links";
    private static final String RETRY_MESSAGE = "Retryable status code: ";
    private static final String TRACK_LINK_ERROR_MESSAGE =
        "Error occurred during trackLink in ScrapperClient! ErrorResponse: ";
    private static final String UNTRACK_LINK_ERROR_MESSAGE =
        "Error occurred during untrackLink in ScrapperClient! ErrorResponse: ";
    private static final String LIST_LINKS_ERROR_MESSAGE =
        "Error occurred during listLinks in ScrapperClient! ErrorResponse: ";
    private static final String ERROR_MESSAGE_TEMPLATE = " - [%d %s] - ScrapperErrorResponse: ";
    public final String defaultApiUrl = "http://localhost:8080";
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final Set<Integer> retryStatusCodes;

    public ScrapperClient(
        String baseApiUrl, RetryTemplate retryTemplate, Set<Integer> retryStatusCodes
    ) {
        this.webClient = WebClient.create(
            baseApiUrl == null || baseApiUrl.isEmpty()
                ? defaultApiUrl
                : baseApiUrl);
        this.retryTemplate = retryTemplate;
        this.retryStatusCodes = retryStatusCodes;
    }

    public void registerTgChat(Long id) {
        retryTemplate.execute(
            context -> registerTgChatWebClientRequest(id).block()
        );
    }

    private Mono<ScrapperErrorResponse> registerTgChatWebClientRequest(Long id) {
        return webClient.post()
            .uri(TG_CHAT_ENDPOINT + "/" + id)
            .exchangeToMono(response -> {
                if (retryStatusCodes.contains(response.statusCode().value())) {
                    return Mono.error(
                        new RetryableRequestException(
                            RETRY_MESSAGE
                                + response.statusCode().value())
                    );
                }
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(ScrapperErrorResponse.class)
                        .flatMap(botResponse -> {
                            if (response.statusCode().value() == (CONFLICT.value())) {
                                return Mono.error(
                                    new DuplicateRegistrationException(botResponse.description())
                                );
                            }
                            if (response.statusCode().value() == (BAD_REQUEST.value())) {
                                return Mono.error(
                                    new InvalidDataException(botResponse.description())
                                );
                            }
                            return Mono.error(
                                new RuntimeException(
                                    "Error occurred during registerTgChat in ScrapperClient!"
                                        + String.format(
                                        ERROR_MESSAGE_TEMPLATE,
                                        response.statusCode().value(),
                                        ((HttpStatus) response.statusCode()).getReasonPhrase()
                                    )
                                        + botResponse
                                )
                            );
                        });
                }
                return Mono.empty();
            });
    }

    public void deleteTgChat(Long id) {
        retryTemplate.execute(
            context -> deleteTgChatWebClientRequest(id).block()
        );
    }

    private Mono<ScrapperErrorResponse> deleteTgChatWebClientRequest(Long id) {
        return webClient.delete()
            .uri(TG_CHAT_ENDPOINT + "/" + id)
            .exchangeToMono(response -> {
                if (retryStatusCodes.contains(response.statusCode().value())) {
                    return Mono.error(
                        new RetryableRequestException(
                            RETRY_MESSAGE
                                + response.statusCode().value())
                    );
                }
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(ScrapperErrorResponse.class)
                        .flatMap(botResponse -> {
                            if (response.statusCode().value() == (NOT_FOUND.value())) {
                                return Mono.error(
                                    new TgChatNotExistException(botResponse.description())
                                );
                            }
                            if (response.statusCode().value() == (BAD_REQUEST.value())) {
                                return Mono.error(
                                    new InvalidDataException(botResponse.description())
                                );
                            }
                            return Mono.error(
                                new RuntimeException(
                                    "Error occurred during deleteTgChat in ScrapperClient!"
                                        + String.format(
                                        ERROR_MESSAGE_TEMPLATE,
                                        response.statusCode().value(),
                                        ((HttpStatus) response.statusCode()).getReasonPhrase()
                                    )
                                        + botResponse
                                )
                            );
                        });
                }
                return Mono.empty();
            });
    }

    public LinkResponse trackLink(Long id, URI url) {
        return retryTemplate.execute(
            context -> trackLinkWebClientRequest(id, url).block()
        );
    }

    public Mono<LinkResponse> trackLinkWebClientRequest(Long id, URI url) {
        return webClient.post()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .bodyValue(new LinkRequest(url))
            .exchangeToMono(response -> {
                if (retryStatusCodes.contains(response.statusCode().value())) {
                    return Mono.error(
                        new RetryableRequestException(
                            RETRY_MESSAGE
                                + response.statusCode().value())
                    );
                }
                if (response.statusCode().equals(OK)) {
                    return response.bodyToMono(LinkResponse.class);
                }
                return response.bodyToMono(ScrapperErrorResponse.class)
                    .flatMap(errorResponse -> {
                        String errorMessage = errorResponse.description();
                        if (response.statusCode().value() == BAD_REQUEST.value()) {
                            return Mono.error(new InvalidDataException(
                                TRACK_LINK_ERROR_MESSAGE + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == CONFLICT.value()) {
                            return Mono.error(new DuplicateLinkPostException(
                                TRACK_LINK_ERROR_MESSAGE + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == NOT_FOUND.value()) {
                            return Mono.error(new TgChatNotExistException(
                                TRACK_LINK_ERROR_MESSAGE + errorMessage
                            ));
                        }
                        return Mono.error(new RuntimeException(
                            TRACK_LINK_ERROR_MESSAGE
                                + String.format(
                                ERROR_MESSAGE_TEMPLATE,
                                response.statusCode().value(),
                                ((HttpStatus) response.statusCode()).getReasonPhrase()
                            )
                                + errorMessage
                        ));
                    });
            });
    }

    public LinkResponse untrackLink(Long id, URI url) {
        return retryTemplate.execute(
            context -> untrackLinkWebClientRequest(id, url).block()
        );
    }

    public Mono<LinkResponse> untrackLinkWebClientRequest(Long id, URI url) {
        return webClient.method(HttpMethod.DELETE)
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .bodyValue(new LinkRequest(url))
            .exchangeToMono(response -> {
                if (retryStatusCodes.contains(response.statusCode().value())) {
                    return Mono.error(new RetryableRequestException(
                        RETRY_MESSAGE
                            + response.statusCode().value())
                    );
                }
                if (response.statusCode().equals(OK)) {
                    return response.bodyToMono(LinkResponse.class);
                }
                return response.bodyToMono(ScrapperErrorResponse.class)
                    .flatMap(errorResponse -> {
                        String errorMessage = errorResponse.description();
                        if (response.statusCode().value() == BAD_REQUEST.value()) {
                            return Mono.error(new InvalidDataException(
                                UNTRACK_LINK_ERROR_MESSAGE + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == NOT_FOUND.value()) {
                            if (errorMessage.equals("Чат не существует")) {
                                return Mono.error(new TgChatNotExistException(
                                    UNTRACK_LINK_ERROR_MESSAGE + errorMessage
                                ));
                            } else {
                                return Mono.error(new LinkNotFoundException(
                                    UNTRACK_LINK_ERROR_MESSAGE + errorMessage
                                ));
                            }
                        }
                        return Mono.error(new RuntimeException(
                            UNTRACK_LINK_ERROR_MESSAGE
                                + String.format(
                                ERROR_MESSAGE_TEMPLATE,
                                response.statusCode().value(),
                                ((HttpStatus) response.statusCode()).getReasonPhrase()
                            )
                                + errorMessage
                        ));
                    });
            });
    }

    public ListLinksResponse listLinks(Long id) {
        return retryTemplate.execute(
            context -> listLinksWebClientRequest(id).block()
        );
    }

    public Mono<ListLinksResponse> listLinksWebClientRequest(Long id) {
        return webClient.get()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .exchangeToMono(response -> {
                if (retryStatusCodes.contains(response.statusCode().value())) {
                    return Mono.error(
                        new RetryableRequestException(
                            RETRY_MESSAGE
                                + response.statusCode().value())
                    );
                }
                if (response.statusCode().equals(OK)) {
                    return response.bodyToMono(ListLinksResponse.class);
                }
                return response.bodyToMono(ScrapperErrorResponse.class)
                    .flatMap(errorResponse -> {
                        String errorMessage = errorResponse.description();
                        if (response.statusCode().value() == BAD_REQUEST.value()) {
                            return Mono.error(new InvalidDataException(
                                LIST_LINKS_ERROR_MESSAGE + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == NOT_FOUND.value()) {
                            return Mono.error(new TgChatNotExistException(
                                LIST_LINKS_ERROR_MESSAGE + errorMessage
                            ));
                        }
                        return Mono.error(new RuntimeException(
                            LIST_LINKS_ERROR_MESSAGE
                                + String.format(
                                ERROR_MESSAGE_TEMPLATE,
                                response.statusCode().value(),
                                ((HttpStatus) response.statusCode()).getReasonPhrase()
                            )
                                + errorMessage
                        ));
                    });
            });
    }

}
