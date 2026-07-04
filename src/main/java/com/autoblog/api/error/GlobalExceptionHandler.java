package com.autoblog.api.error;

import com.autoblog.application.DuplicateVinException;
import com.autoblog.application.InvalidVinException;
import com.autoblog.application.VehicleNotFoundException;
import com.autoblog.publicreport.domain.PublicReportNotFoundException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(InvalidVinException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVin(InvalidVinException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            VehicleNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(PublicReportNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePublicReportNotFound(
            PublicReportNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateVinException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateVin(
            DuplicateVinException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "Request conflicts with existing data", request, List.of());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> details = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorDetail(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Request parameter is invalid", request, List.of(
                new FieldErrorDetail(exception.getName(), "Invalid value")
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        Throwable cause = exception.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            return handleInvalidFormat(invalidFormatException, request);
        }
        if (cause instanceof MismatchedInputException mismatchedInputException) {
            return build(HttpStatus.BAD_REQUEST, "Validation failed", request, List.of(
                    new FieldErrorDetail(fieldPath(mismatchedInputException), "Invalid JSON value for this field")
            ));
        }
        if (cause instanceof JsonParseException) {
            return build(HttpStatus.BAD_REQUEST, "Validation failed", request, List.of(
                    new FieldErrorDetail("body", "Malformed JSON request body")
            ));
        }

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, List.of(
                new FieldErrorDetail("body", "Request body is invalid or missing")
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorDetail> details
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        ));
    }

    private ResponseEntity<ApiErrorResponse> handleInvalidFormat(
            InvalidFormatException exception,
            HttpServletRequest request
    ) {
        Class<?> targetType = exception.getTargetType();
        if (targetType != null && targetType.isEnum()) {
            return build(HttpStatus.BAD_REQUEST, "Validation failed", request, List.of(
                    new FieldErrorDetail(
                            fieldPath(exception),
                            unsupportedEnumMessage(exception.getValue(), targetType)
                    )
            ));
        }

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, List.of(
                new FieldErrorDetail(fieldPath(exception), "Invalid value: " + exception.getValue())
        ));
    }

    private String unsupportedEnumMessage(Object value, Class<?> enumType) {
        String supportedValues = Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return "Unsupported event type: " + value + ". Supported values: " + supportedValues;
    }

    private String fieldPath(JsonMappingException exception) {
        String path = exception.getPath().stream()
                .map(reference -> reference.getFieldName() != null
                        ? reference.getFieldName()
                        : "[" + reference.getIndex() + "]")
                .collect(Collectors.joining("."));
        return path.isBlank() ? "body" : path;
    }
}
