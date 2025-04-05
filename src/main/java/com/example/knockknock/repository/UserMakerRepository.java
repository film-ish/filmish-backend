package com.example.knockknock.repository;

import com.example.knockknock.entity.UserMaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMakerRepository extends JpaRepository<UserMaker, Long> {
    UserMaker findUserMakerByMakerId(Long makerId);
}
