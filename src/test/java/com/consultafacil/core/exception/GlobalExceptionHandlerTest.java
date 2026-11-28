package com.consultafacil.core.exception;

import com.consultafacil.domain.exception.DomainException;
import com.consultafacil.domain.exception.InvalidStateException;
import com.consultafacil.domain.exception.PaymentRequiredException;
import com.consultafacil.domain.exception.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler;
    WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new ServletWebRequest(new MockHttpServletRequest("GET", "/test"));
    }

    @Test
    void handleResourceNotFound_returns404() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("Appointment", "id-1"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("Appointment");
    }

    @Test
    void handleResourceNotFound_singleMessageConstructor_returns404() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("Not found"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Not found");
    }

    @Test
    void handleDuplicateResource_returns409() {
        var response = handler.handleDuplicateResource(
                new DuplicateResourceException("User", "email", "test@example.com"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("User");
    }

    @Test
    void handleDuplicateResource_singleMessageConstructor_returns409() {
        var response = handler.handleDuplicateResource(
                new DuplicateResourceException("Already exists"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Already exists");
    }

    @Test
    void handlePaymentRequired_returns402WithRequiredPlanField() {
        var response = handler.handlePaymentRequired(new PaymentRequiredException("PRO"), request);
        assertThat(response.getStatusCode().value()).isEqualTo(402);
        assertThat(response.getBody()).containsEntry("requiredPlan", "PRO");
        assertThat(response.getBody()).containsKey("upgradeUrl");
    }

    @Test
    void handleRateLimit_returns429() {
        var response = handler.handleRateLimit(
                new RateLimitException("RATE_LIMIT_AUTH", "Too many requests"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody().getMessage()).isEqualTo("Too many requests");
    }

    @Test
    void handleDomainException_returns400() {
        var response = handler.handleDomainException(new InvalidStateException("Bad state"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleBadRequest_returns400() {
        var response = handler.handleBadRequest(new BadRequestException("Invalid input"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input");
    }

    @Test
    void handleUnauthorized_returns401() {
        var response = handler.handleUnauthorized(new UnauthorizedException("Not allowed"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleWebhookAuth_returns401WithFixedMessage() {
        var response = handler.handleWebhookAuth(
                new WebhookAuthenticationException("Bad signature"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid webhook signature");
    }

    @Test
    void handleValidationExceptions_returns400WithErrorsList() throws Exception {
        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "email", "must not be blank"));
        bindingResult.addError(new FieldError("target", "name", "size must be between 2 and 100"));

        var ex = new MethodArgumentNotValidException(null, bindingResult);
        var response = handler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors())
                .anyMatch(e -> e.contains("email"))
                .anyMatch(e -> e.contains("name"));
    }

    @Test
    void handleAccessDenied_returns403() {
        var response = handler.handleAccessDenied(new AccessDeniedException("Forbidden"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).isEqualTo("Acesso negado");
    }

    @Test
    void handleGlobalException_returns500() {
        var response = handler.handleGlobalException(new RuntimeException("Unexpected"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
    }

    @Test
    void errorResponse_convenienceConstructor_setsAllFields() {
        var error = new ErrorResponse(HttpStatus.NOT_FOUND, "not found", "/api/test");
        assertThat(error.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(error.getStatusCode()).isEqualTo(404);
        assertThat(error.getMessage()).isEqualTo("not found");
        assertThat(error.getPath()).isEqualTo("/api/test");
        assertThat(error.getTimestamp()).isNotNull();
    }
}
