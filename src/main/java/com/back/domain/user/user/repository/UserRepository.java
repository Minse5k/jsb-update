package com.back.domain.user.user.repository;

import com.back.domain.user.user.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Integer> {
    Optional<SiteUser> findByUsername(String username); // WHERE username = ?

    Optional<SiteUser> findByEmail(String email); // WHERE email = ?
}
