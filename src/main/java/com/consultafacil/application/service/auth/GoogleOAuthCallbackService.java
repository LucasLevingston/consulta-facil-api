package com.consultafacil.application.service.auth;

import com.consultafacil.api.dto.auth.LoginResponseDTO;
import com.consultafacil.application.port.in.GoogleOAuthCallbackUseCase;
import com.consultafacil.core.config.GoogleOAuthProperties;
import com.consultafacil.core.security.JwtTokenProvider;
import com.consultafacil.core.util.PiiMask;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.GoogleOAuthPort;
import com.consultafacil.domain.port.out.GoogleOAuthPort.GoogleUserInfo;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthCallbackService implements GoogleOAuthCallbackUseCase {

    private final GoogleOAuthPort googleOAuthPort;
    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CreateRefreshTokenService createRefreshTokenService;
    private final GoogleOAuthProperties googleProps;

    @Override
    @Transactional
    public LoginResponseDTO execute(String code) {
        GoogleUserInfo info = googleOAuthPort.exchangeCode(code, googleProps.getRedirectUri());

        User user = userRepository.findByGoogleId(info.sub())
                .orElseGet(() -> findOrCreateByEmail(info));

        if (user.getGoogleId() == null) {
            user.setGoogleId(info.sub());
            user = userRepository.save(user);
        }

        String jwt = jwtTokenProvider.generateToken(user);
        String refreshToken = createRefreshTokenService.createFor(user).getToken();
        log.info("[GoogleOAuth] Callback login userId={} email={}", user.getId(), PiiMask.maskEmail(user.getEmail()));
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
        log.info("[GoogleOAuth] New user via callback userId={}", user.getId());
        return user;
    }
}
