package com.consultafacil.application.service;

import com.consultafacil.api.dto.auth.LoginRequestDTO;
import com.consultafacil.api.dto.auth.LoginResponseDTO;
import com.consultafacil.core.exception.UnauthorizedException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.consultafacil.application.port.in.LoginUseCase;
import com.consultafacil.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase {

    private static final String DUMMY_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${auth.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Override
    public LoginResponseDTO execute(LoginRequestDTO request) {
        return login(request);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            // Timing normalization — prevent email enumeration via response time
            passwordEncoder.matches(request.getPassword(), DUMMY_HASH);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.isCurrentlyLocked()) {
            throw new UnauthorizedException("Account temporarily locked. Try again later.");
        }

        if (user.getPassword() == null) {
            // Google-only account — prevent timing oracle via dummy hash
            passwordEncoder.matches(request.getPassword(), DUMMY_HASH);
            throw new UnauthorizedException("This account uses Google login. Please use 'Sign in with Google'.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.recordFailedLogin(maxAttempts, lockoutDurationMinutes);
            if (user.isCurrentlyLocked()) {
                log.warn("Account locked after {} failed attempts: userId={}", user.getFailedLoginAttempts(), user.getId());
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.resetLoginAttempts();
            userRepository.save(user);
        }

        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createFor(user).getToken();

        return LoginResponseDTO.of(token, refreshToken, jwtTokenProvider.getExpiresIn(),
                user.getId(), user.getEmail(), user.getRole());
    }
}
