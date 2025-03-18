package com.example.knockknock.domain.repository;

import com.example.knockknock.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
