package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
