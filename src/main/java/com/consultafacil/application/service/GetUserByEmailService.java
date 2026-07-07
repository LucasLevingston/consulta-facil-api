package com.consultafacil.application.service;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.GetUserByEmailUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.util.PiiMask;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserByEmailService implements GetUserByEmailUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO execute(String email) {
        log.debug("Fetching user by email: {}", PiiMask.maskEmail(email));
        return mapper.toResponseDTO(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found")));
    }
}
