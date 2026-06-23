package com.example.unitaskerbackend.repository;

import com.example.unitaskerbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // YENİ: E-posta adresi üzerinden kullanıcıyı filtreleme (Login işlemi için kritik)
    Optional<User> findByEmail(String email);
}