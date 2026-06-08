package com.consultafacil.core.exception;

import com.consultafacil.domain.exception.InvalidStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
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

    @Test void handleResourceNotFound_returns404() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("Appointment", "id-1"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("Appointment");
    }

    @Test void handleBadRequest_returns400() {
        var response = handler.handleBadRequest(new BadRequestException("Invalid input"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input");
    }

    @Test void handleDomainException_returns400() {
        var response = handler.handleDomainException(
                new InvalidStateException("Bad state"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test void handleUnauthorized_returns401() {
        var response = handler.handleUnauthorized(new UnauthorizedException("Not allowed"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test void handleGlobalException_returns500() {
        var response = handler.handleGlobalException(new RuntimeException("Unexpected"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test void handleWebhookAuth_returns401() {
        var response = handler.handleWebhookAuth(
                new WebhookAuthenticationException("Bad signature"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
