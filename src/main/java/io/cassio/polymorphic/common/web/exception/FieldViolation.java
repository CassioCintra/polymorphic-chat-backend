package io.cassio.polymorphic.common.web.exception;

public record FieldViolation(
        String field,
        String message
) {}
