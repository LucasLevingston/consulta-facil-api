package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<User> findByPhone(String phone);

    Optional<User> findByGoogleId(String googleId);

    void delete(User user);
}
