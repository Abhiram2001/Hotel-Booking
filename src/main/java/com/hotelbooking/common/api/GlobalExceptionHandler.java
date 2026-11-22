package com.hotelbooking.common.api;

import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.common.domain.error.DomainException;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.common.domain.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turns exceptions into RFC 9457 problem responses. The domain decides the category of an error; only this
 * class knows how a category maps to an HTTP status.
 */
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Business rejections are expected outcomes, so they are logged at DEBUG; the request line shows the status. */
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        HttpStatus status = statusFor(ex);
        log.debug("Request rejected with {} {}: {}", status.value(), ex.code(), ex.getMessage());
        return problem(status, ex.code(), ex.getMessage());
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> handleLockTimeout(PessimisticLockingFailureException ex) {
        log.warn("Lock not acquired in time", ex);
        return problem(HttpStatus.CONFLICT, "RESOURCE_BUSY", "The resource is being updated by another request. Retry shortly.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return problem(HttpStatus.CONFLICT, "DATA_CONFLICT", "The request conflicts with existing data.");
    }

    /**
     * Anything not handled above is a bug. It is logged here, while the request id is still in the logging
     * context, and the client gets a generic message instead of internal details.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unexpected error while handling the request", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Quote the X-Request-Id response header when reporting it.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ex.getBody();
        body.setProperty("code", "VALIDATION_FAILED");
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.requireNonNullElse(error.getDefaultMessage(), "is invalid")))
                .toList();
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /** Framework errors (unreadable body, missing header, bad path variable) also get a {@code code}. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem && !hasCode(problem)) {
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            problem.setProperty("code", status == null ? "REQUEST_FAILED" : status.name());
        }
        return response;
    }

    private static boolean hasCode(ProblemDetail problem) {
        return problem.getProperties() != null && problem.getProperties().containsKey("code");
    }

    private static HttpStatus statusFor(DomainException ex) {
        if (ex instanceof DomainValidationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ConflictException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof BusinessRuleViolationException) {
            return HttpStatus.UNPROCESSABLE_CONTENT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        body.setProperty("code", code);
        return ResponseEntity.status(status).body(body);
    }
}
