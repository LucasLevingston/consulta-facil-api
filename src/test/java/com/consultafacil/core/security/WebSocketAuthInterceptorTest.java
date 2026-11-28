package com.consultafacil.core.security;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    CustomUserDetailsService userDetailsService;

    @Mock
    MessageChannel channel;

    WebSocketAuthInterceptor interceptor;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String USER_ID = "user-abc-123";

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(jwtTokenProvider, userDetailsService);
    }

    private CustomUserDetails userDetails() {
        User user = User.builder()
                .id(USER_ID)
                .email("test@email.com")
                .name("Test User")
                .password("hashed")
                .role(UserRole.PATIENT)
                .build();
        return new CustomUserDetails(user);
    }

    private Message<?> connectMessageWithToken(String bearerToken) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (bearerToken != null) {
            accessor.addNativeHeader("Authorization", bearerToken);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<?> nonConnectMessage(StompCommand command) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    // ── CONNECT with valid token ──────────────────────────────────────────

    @Test
    void preSend_connectWithValidToken_setsUserOnAccessor() {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(USER_ID);
        when(userDetailsService.loadUserById(USER_ID)).thenReturn(userDetails());

        Message<?> message = connectMessageWithToken("Bearer " + VALID_TOKEN);
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        Authentication auth = (Authentication) resultAccessor.getUser();
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    void preSend_connectWithValidToken_authoritiesAreSet() {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(USER_ID);
        when(userDetailsService.loadUserById(USER_ID)).thenReturn(userDetails());

        Message<?> message = connectMessageWithToken("Bearer " + VALID_TOKEN);
        interceptor.preSend(message, channel);

        verify(userDetailsService).loadUserById(USER_ID);
    }

    // ── CONNECT with invalid token ────────────────────────────────────────

    @Test
    void preSend_connectWithInvalidToken_userNotSet() {
        when(jwtTokenProvider.validateToken(VALID_TOKEN)).thenReturn(false);

        Message<?> message = connectMessageWithToken("Bearer " + VALID_TOKEN);
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
        verify(userDetailsService, never()).loadUserById(any());
    }

    @Test
    void preSend_connectWithNoAuthorizationHeader_userNotSet() {
        Message<?> message = connectMessageWithToken(null);
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    void preSend_connectWithBearerPrefixOnly_userNotSet() {
        // Header value is just "Bearer " with no actual token
        Message<?> message = connectMessageWithToken("Bearer ");
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
        // empty string after "Bearer " is blank, so no validation happens
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    void preSend_connectWithMissingBearerPrefix_tokenNotValidated() {
        Message<?> message = connectMessageWithToken("Basic dXNlcjpwYXNz");
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isNotNull();
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    // ── Non-CONNECT commands pass through without authentication ─────────

    @Test
    void preSend_subscribeCommand_returnsMessageUnchanged() {
        Message<?> message = nonConnectMessage(StompCommand.SUBSCRIBE);
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }

    @Test
    void preSend_sendCommand_returnsMessageUnchanged() {
        Message<?> message = nonConnectMessage(StompCommand.SEND);
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }

    @Test
    void preSend_disconnectCommand_returnsMessageUnchanged() {
        Message<?> message = nonConnectMessage(StompCommand.DISCONNECT);
        Message<?> result = interceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }
}
