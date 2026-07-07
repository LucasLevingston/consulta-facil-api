package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.application.port.in.user.GetUserByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserByIdService implements GetUserByIdUseCase {

    private final UserRepositoryPort userRepository;
    private final UserMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO execute(String id) {
        log.debug("Fetching user by ID: {}", id);
        return mapper.toResponseDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }
}
