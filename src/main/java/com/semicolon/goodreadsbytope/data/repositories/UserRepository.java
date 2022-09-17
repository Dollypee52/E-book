package com.semicolon.goodreadsbytope.data.repositories;

import com.semicolon.goodreadsbytope.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
}
