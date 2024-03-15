package edu.eflerrr.bot.controller;

import edu.eflerrr.bot.controller.dto.request.LinkUpdate;
import edu.eflerrr.bot.controller.dto.response.ApiErrorResponse;
import edu.eflerrr.bot.service.UpdatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.MalformedURLException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@Tag(name = "updates", description = "the updates API")
@RestController
@RequiredArgsConstructor
public class UpdatesController {

    private final UpdatesService updatesService;

    /**
     * POST /updates : Отправить обновление
     *
     * @param linkUpdate (required)
     * @return Обновление обработано (status code 200)
     *     or Некорректные параметры запроса (status code 400)
     */
    @Operation(
        operationId = "updatesPost",
        summary = "Отправить обновление",
        responses = {
            @ApiResponse(responseCode = "200", description = "Обновление обработано"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса", content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/updates",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> updatesPost(
        @Parameter(name = "LinkUpdate",
                   description = "Обновленный ресурс и id подписанных telegram-чатов",
                   required = true)
        @RequestBody
        LinkUpdate linkUpdate
    ) throws MalformedURLException {
        updatesService.processUpdate(
            linkUpdate.getId(),
            linkUpdate.getUrl().toURL(),
            linkUpdate.getDescription(),
            linkUpdate.getTgChatIds()
        );
        return ResponseEntity.ok().build();
    }

}
