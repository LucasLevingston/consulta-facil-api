package com.consultafacil.application.service.user;

import com.consultafacil.application.port.in.user.AnonymizeUserUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnonymizeUserService implements AnonymizeUserUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public void execute(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        DeleteUserService.anonymize(user);
        userRepository.save(user);
    }
}
