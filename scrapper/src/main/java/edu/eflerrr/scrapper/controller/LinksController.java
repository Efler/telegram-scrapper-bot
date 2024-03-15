package edu.eflerrr.scrapper.controller;

import edu.eflerrr.scrapper.controller.dto.request.AddLinkRequest;
import edu.eflerrr.scrapper.controller.dto.request.RemoveLinkRequest;
import edu.eflerrr.scrapper.controller.dto.response.ApiErrorResponse;
import edu.eflerrr.scrapper.controller.dto.response.LinkResponse;
import edu.eflerrr.scrapper.controller.dto.response.ListLinksResponse;
import edu.eflerrr.scrapper.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@Tag(name = "links", description = "the links API")
@RestController
@RequiredArgsConstructor
public class LinksController {

    private final LinkService linkService;

    /**
     * DELETE /links : Убрать отслеживание ссылки
     *
     * @param tgChatId          (required)
     * @param removeLinkRequest (required)
     * @return Ссылка успешно убрана (status code 200)
     *     or Некорректные параметры запроса (status code 400)
     *     or Ссылка не найдена (status code 404)
     */
    @Operation(
        operationId = "linksDelete",
        summary = "Убрать отслеживание ссылки",
        responses = {
            @ApiResponse(responseCode = "200", description = "Ссылка успешно убрана", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = LinkResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Ссылка не найдена или чат не существует", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/links",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LinkResponse> linksDelete(
        @NotNull
        @Parameter(name = "Tg-Chat-Id", description = "id Telegram чата", required = true)
        @RequestHeader(value = "Tg-Chat-Id", required = true)
        Long tgChatId,
        @Parameter(name = "RemoveLinkRequest", description = "Информация о ссылке", required = true)
        @RequestBody
        RemoveLinkRequest removeLinkRequest
    ) {
        var deletedLink = linkService.delete(tgChatId, removeLinkRequest.getLink());
        return ResponseEntity.ok().body(
            new LinkResponse()
                .id(deletedLink.getId())
                .url(deletedLink.getUrl())
        );
    }

    /**
     * GET /links : Получить все отслеживаемые ссылки
     *
     * @param tgChatId (required)
     * @return Ссылки успешно получены (status code 200)
     *     or Некорректные параметры запроса (status code 400)
     */
    @Operation(
        operationId = "linksGet",
        summary = "Получить все отслеживаемые ссылки",
        responses = {
            @ApiResponse(responseCode = "200", description = "Ссылки успешно получены", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ListLinksResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Чат не существует", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/links",
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListLinksResponse> linksGet(
        @NotNull
        @Parameter(name = "Tg-Chat-Id", description = "id Telegram чата", required = true)
        @RequestHeader(value = "Tg-Chat-Id", required = true)
        Long tgChatId
    ) {
        var links = linkService.listAll(tgChatId);
        var response = new ListLinksResponse();
        if (links.isEmpty()) {
            response.setLinks(new ArrayList<>());
            response.size(0);
        } else {
            for (var link : links) {
                response.addLinksItem(
                    new LinkResponse()
                        .id(link.getId())
                        .url(link.getUrl())
                );
            }
            response.size(links.size());
        }
        return ResponseEntity.ok().body(response);
    }

    /**
     * POST /links : Добавить отслеживание ссылки
     *
     * @param tgChatId       (required)
     * @param addLinkRequest (required)
     * @return Ссылка успешно добавлена (status code 200)
     *     or Некорректные параметры запроса (status code 400)
     */
    @Operation(
        operationId = "linksPost",
        summary = "Добавить отслеживание ссылки",
        responses = {
            @ApiResponse(responseCode = "200", description = "Ссылка успешно добавлена", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = LinkResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Чат не существует", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            }),
            @ApiResponse(responseCode = "409", description = "Повторное добавление ссылки", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/links",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LinkResponse> linksPost(
        @NotNull
        @Parameter(name = "Tg-Chat-Id", description = "id Telegram чата", required = true)
        @RequestHeader(value = "Tg-Chat-Id", required = true)
        Long tgChatId,
        @Parameter(name = "AddLinkRequest", description = "Информация о ссылке", required = true)
        @RequestBody
        AddLinkRequest addLinkRequest
    ) {
        var addedLink = linkService.add(tgChatId, addLinkRequest.getLink());
        return ResponseEntity.ok().body(
            new LinkResponse()
                .id(addedLink.getId())
                .url(addedLink.getUrl())
        );
    }

}
