package io.cassio.polymorphic.common.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String type,
        String title,
        Integer status,
        String detail,
        String instance,
        @JsonProperty("error_code")
        String errorCode,
        Instant timestamp,
        List<FieldViolation> violations
) {

    private static String typeFromCode(String code) {
        return "urn:polymorphic:problem:" + code.toLowerCase().replace("_", "-");
    }

    public static ErrorResponse of(String code, int status, String title, String detail, String instance) {
        return new ErrorResponse(
                typeFromCode(code),
                title,
                status,
                detail,
                instance,
                code,
                Instant.now(),
                null
        );
    }

    public static ErrorResponse validation(String instance, List<FieldViolation> violations) {
        return new ErrorResponse(
                typeFromCode("VALIDATION_ERROR"),
                "Bad Request",
                400,
                "Validation failed.",
                instance,
                "VALIDATION_ERROR",
                Instant.now(),
                violations
        );
    }
}
