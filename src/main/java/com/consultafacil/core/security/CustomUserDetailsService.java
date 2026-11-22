package com.consultafacil.core.security;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));
        return new CustomUserDetails(user);
    }

    public CustomUserDetails loadUserById(String userId) {
        // Tokens of anonymized accounts stop working immediately (subject is the user id).
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com ID: " + userId));
        return new CustomUserDetails(user);
    }
}
