package com.consultafacil.application.service.user;

import com.consultafacil.application.port.in.user.DeleteUserUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public void execute(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        anonymize(user);
        userRepository.save(user);
    }

    static void anonymize(User user) {
        user.setName("Usuário Removido");
        user.setEmail("deleted-" + user.getId() + "@removed.consultafacil.com");
        user.setPassword(null);
        user.setGoogleId(null);
        user.setCpf(null);
        user.setPhone(null);
        user.setBirthDate(null);
        user.setGender(null);
        user.setImageUrl(null);
        user.setImageId(null);
        user.setDeletedAt(LocalDateTime.now());
    }
}
