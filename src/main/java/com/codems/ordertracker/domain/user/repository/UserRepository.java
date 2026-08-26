package com.codems.ordertracker.domain.user.repository;

import com.codems.ordertracker.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndEnabledTrue(Long id);

    boolean existsByEmail(String email);

}
