package edu.eflerrr.scrapper.advice;

import edu.eflerrr.scrapper.controller.dto.response.ApiErrorResponse;
import edu.eflerrr.scrapper.exception.DuplicateLinkPostException;
import edu.eflerrr.scrapper.exception.DuplicateRegistrationException;
import edu.eflerrr.scrapper.exception.LinkNotFoundException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(TgChatNotExistException.class)
    public ResponseEntity<ApiErrorResponse> tgChatNotExist(TgChatNotExistException ex) {
        var response = new ApiErrorResponse()
            .description("Чат не существует")
            .code(Integer.toString(NOT_FOUND.value()))
            .exceptionName(ex.getClass().getName())
            .exceptionMessage(ex.getMessage());
        for (var stackTraceElement : ex.getStackTrace()) {
            response.addStacktraceItem(stackTraceElement.toString());
        }
        return ResponseEntity.status(NOT_FOUND).body(response);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> linkNotFound(LinkNotFoundException ex) {
        var response = new ApiErrorResponse()
            .description("Ссылка не найдена")
            .code(Integer.toString(NOT_FOUND.value()))
            .exceptionName(ex.getClass().getName())
            .exceptionMessage(ex.getMessage());
        for (var stackTraceElement : ex.getStackTrace()) {
            response.addStacktraceItem(stackTraceElement.toString());
        }
        return ResponseEntity.status(NOT_FOUND).body(response);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, NullPointerException.class})
    public ResponseEntity<ApiErrorResponse> invalidRequestData(Exception ex) {
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

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<ApiErrorResponse> duplicateRegistration(DuplicateRegistrationException ex) {
        var response = new ApiErrorResponse()
            .description("Повторная регистрация")
            .code(Integer.toString(CONFLICT.value()))
            .exceptionName(ex.getClass().getName())
            .exceptionMessage(ex.getMessage());
        for (var stackTraceElement : ex.getStackTrace()) {
            response.addStacktraceItem(stackTraceElement.toString());
        }
        return ResponseEntity.status(CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicateLinkPostException.class)
    public ResponseEntity<ApiErrorResponse> duplicateLinkPost(DuplicateLinkPostException ex) {
        var response = new ApiErrorResponse()
            .description("Повторное добавление ссылки")
            .code(Integer.toString(CONFLICT.value()))
            .exceptionName(ex.getClass().getName())
            .exceptionMessage(ex.getMessage());
        for (var stackTraceElement : ex.getStackTrace()) {
            response.addStacktraceItem(stackTraceElement.toString());
        }
        return ResponseEntity.status(CONFLICT).body(response);
    }
}
