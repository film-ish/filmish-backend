package com.example.knockknock.repository;

import com.example.knockknock.entity.UserMaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMakerRepository extends JpaRepository<UserMaker, Long> {
    //TODO
    // 이름을 findByMakerId로 변경하는 게 어떨지 얘기해보기!
    UserMaker findUserMakerByMakerId(Long makerId);
}
