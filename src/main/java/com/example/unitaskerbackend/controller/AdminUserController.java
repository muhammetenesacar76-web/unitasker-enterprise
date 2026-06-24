package com.example.unitaskerbackend.controller;

import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    // 1. Admin içeriden direkt kullanıcı oluşturur (OTP/E-posta doğrulaması otomatik bypass edilir!)
    @PostMapping("/add")
    public ResponseEntity<String> adminAddUser(@RequestBody User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use!");
        }

        // Admin oluşturduğu için sistem otomatik onaylı sayar
        newUser.setVerified(true);
        newUser.setLevel(1);
        newUser.setXp(0);

        if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
            newUser.setRole("ROLE_USER");
        }

        userRepository.save(newUser);
        return ResponseEntity.ok("User created successfully by Admin!");
    }

    // 2. Admin sistemden kalıcı olarak kullanıcı ve verilerini siler
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> adminDeleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // 3. Admin kullanıcı bilgilerini düzenler (İsim, E-posta, Rol veya Şifre)
    @PutMapping("/update/{id}")
    public ResponseEntity<String> adminUpdateUser(@PathVariable Long id, @RequestBody User updatedData) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        user.setFullName(updatedData.getFullName());
        user.setEmail(updatedData.getEmail());

        if (updatedData.getRole() != null && !updatedData.getRole().isEmpty()) {
            user.setRole(updatedData.getRole());
        }

        // Eğer şifre alanı ön yüzden dolu geldiyse güncelle
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            user.setPassword(updatedData.getPassword());
        }

        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully!");
    }
}
