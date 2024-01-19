package com.ssafy.backend.user.repository;

import com.ssafy.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findUserByUserNickname(String userTriedNickname);
}
