package com.example.consulta.application.service;

import com.example.consulta.api.dto.auth.LoginRequestDTO;
import com.example.consulta.api.dto.auth.LoginResponseDTO;
import com.example.consulta.core.exception.UnauthorizedException;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.repository.UserRepository;
import com.example.consulta.core.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${auth.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            // Timing normalization — prevent email enumeration via response time
            passwordEncoder.matches(request.getPassword(), DUMMY_HASH);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException("Account temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
                log.warn("Account locked after {} failed attempts: userId={}", attempts, user.getId());
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        String token = jwtTokenProvider.generateToken(user);
        Long expiresIn = jwtTokenProvider.getExpiresIn();

        return LoginResponseDTO.of(token, expiresIn, user.getId(), user.getEmail(), user.getRole());
    }
}
