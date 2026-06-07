package com.example.consulta.application.service;

import com.example.consulta.api.dto.auth.LoginResponseDTO;
import com.example.consulta.application.port.in.GoogleLoginUseCase;
import com.example.consulta.core.util.PiiMask;
import com.example.consulta.core.security.JwtTokenProvider;
import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.UserRole;
import com.example.consulta.domain.port.out.GoogleOAuthPort;
import com.example.consulta.domain.port.out.GoogleOAuthPort.GoogleUserInfo;
import com.example.consulta.domain.port.out.PatientProfileRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleLoginService implements GoogleLoginUseCase {

    private final GoogleOAuthPort googleOAuthPort;
    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LoginResponseDTO execute(String idToken) {
        GoogleUserInfo info = googleOAuthPort.verifyIdToken(idToken);

        User user = userRepository.findByGoogleId(info.sub())
                .orElseGet(() -> findOrCreateByEmail(info));

        // link googleId if account existed by email but had no googleId yet
        if (user.getGoogleId() == null) {
            user.setGoogleId(info.sub());
            user = userRepository.save(user);
        }

        String jwt = jwtTokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createFor(user).getToken();
        log.info("[GoogleOAuth] Login userId={} email={}", user.getId(), PiiMask.maskEmail(user.getEmail()));
        return LoginResponseDTO.of(jwt, refreshToken, jwtTokenProvider.getExpiresIn(),
                user.getId(), user.getEmail(), user.getRole());
    }

    private User findOrCreateByEmail(GoogleUserInfo info) {
        return userRepository.findByEmail(info.email())
                .orElseGet(() -> createGoogleUser(info));
    }

    private User createGoogleUser(GoogleUserInfo info) {
        User user = userRepository.save(User.builder()
                .name(info.name())
                .email(info.email())
                .googleId(info.sub())
                .imageUrl(info.picture())
                .role(UserRole.PATIENT)
                .build());

        patientProfileRepository.save(PatientProfile.builder().user(user).build());
        log.info("[GoogleOAuth] New user created userId={}", user.getId());
        return user;
    }
}
