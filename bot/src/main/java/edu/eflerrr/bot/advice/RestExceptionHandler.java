package edu.eflerrr.bot.advice;

import edu.eflerrr.bot.controller.dto.response.ApiErrorResponse;
import edu.eflerrr.bot.exception.InvalidRequestDataException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(InvalidRequestDataException.class)      // TODO: stub!
    public ResponseEntity<ApiErrorResponse> invalidRequestData(InvalidRequestDataException ex) {
        var response = new ApiErrorResponse()
            .description("Некорректные параметры запроса")
            .code(Integer.toString(BAD_REQUEST.value()))
            .exceptionName(ex.getClass().getName())
            .exceptionMessage(ex.getMessage());
        for (var stackTraceElement : ex.getStackTrace()) {
            response.addStacktraceItem(stackTraceElement.toString());
        }
        return ResponseEntity.status(BAD_REQUEST).body(response);
    }
}
