package com.consultafacil.domain.port.out.user;

import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<User> findByPhone(String phone);

    Optional<User> findByGoogleId(String googleId);

    void delete(User user);

    Page<User> findAll(Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);
}
