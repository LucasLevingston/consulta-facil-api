package com.consultafacil.application.service;

import com.consultafacil.domain.entity.ReferralCode;
import com.consultafacil.domain.port.out.ReferralCodeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ReferralCodeGenerator {

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReferralCodeRepositoryPort referralCodeRepository;

    public ReferralCode generateAndSave(String userId) {
        String code = generateUniqueCode();
        return referralCodeRepository.save(ReferralCode.builder()
                .userId(userId)
                .code(code)
                .build());
    }

    private String generateUniqueCode() {
        for (int attempts = 0; attempts < 10; attempts++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!referralCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Não foi possível gerar código único");
    }
}
