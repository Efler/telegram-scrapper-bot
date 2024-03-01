package edu.eflerrr.scrapper.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * ApiErrorResponse
 */

@Setter
@EqualsAndHashCode
@ToString
public class ApiErrorResponse {

    @JsonProperty("description")
    private String description;

    @JsonProperty("code")
    private String code;

    @JsonProperty("exceptionName")
    private String exceptionName;

    @JsonProperty("exceptionMessage")
    private String exceptionMessage;

    @JsonProperty("stacktrace")
    private List<String> stacktrace = null;

    public ApiErrorResponse description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description
     *
     * @return description
     */

    @Schema(name = "description", example = "Ресурс не найден")
    public String getDescription() {
        return description;
    }

    public ApiErrorResponse code(String code) {
        this.code = code;
        return this;
    }

    /**
     * Get code
     *
     * @return code
     */

    @Schema(name = "code", example = "404")
    public String getCode() {
        return code;
    }

    public ApiErrorResponse exceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
        return this;
    }

    /**
     * Get exceptionName
     *
     * @return exceptionName
     */

    @Schema(name = "exceptionName", example = "ResourceNotFoundException")
    public String getExceptionName() {
        return exceptionName;
    }

    public ApiErrorResponse exceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }

    /**
     * Get exceptionMessage
     *
     * @return exceptionMessage
     */

    @Schema(name = "exceptionMessage", example = "Not Found")
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public ApiErrorResponse stacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
        return this;
    }

    public ApiErrorResponse addStacktraceItem(String stacktraceItem) {
        if (this.stacktrace == null) {
            this.stacktrace = new ArrayList<>();
        }
        this.stacktrace.add(stacktraceItem);
        return this;
    }

    /**
     * Get stacktrace
     *
     * @return stacktrace
     */

    @Schema(name = "stacktrace", nullable = true)
    public List<String> getStacktrace() {
        return stacktrace;
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
