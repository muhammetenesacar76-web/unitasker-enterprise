package com.example.unitaskerbackend.controller;

import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.UserRepository;
import com.example.unitaskerbackend.security.JwtUtil;
import com.example.unitaskerbackend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        boolean isAdmin = "admin@unitasker.com".equalsIgnoreCase(user.getEmail());
        user.setRole(isAdmin ? "ROLE_ADMIN" : "ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (isAdmin) {
            // DÜZELTİLDİ: Admin ise doğrudan onaylı yap, mail gönderme külfetinden kurtul!
            user.setVerified(true);
            userRepository.save(user);
            return ResponseEntity.ok("ADMIN_CREATED");
        } else {
            String otpCode = String.format("%06d", new Random().nextInt(999999));
            user.setVerificationCode(otpCode);
            user.setVerified(false);
            user.setOtpCreationTime(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            emailService.sendVerificationCode(savedUser.getEmail(), otpCode);
            return ResponseEntity.ok("OTP_SENT");
        }
    }

    // YENİ: Şifremi Unuttum - Kod Gönderme Kapısı
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetCode = String.format("%06d", new Random().nextInt(999999));
            user.setResetCode(resetCode);
            user.setResetCodeCreationTime(LocalDateTime.now());
            userRepository.save(user);

            emailService.sendPasswordResetCode(email, resetCode);
            return ResponseEntity.ok("RESET_CODE_SENT");
        }
        return ResponseEntity.status(444).body("Error: Email address not found!");
    }

    // YENİ: Kodu Doğrulayıp Yeni Şifreyi Kaydetme Kapısı
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (code != null && code.equals(user.getResetCode())) {
                // 5 dakikalık süre sınırı kontrolü
                if (user.getResetCodeCreationTime() != null && LocalDateTime.now().isAfter(user.getResetCodeCreationTime().plusMinutes(5))) {
                    return ResponseEntity.status(400).body("Error: Reset code has expired!");
                }

                // Yeni şifreyi hash'leyerek üzerine yazıyoruz
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetCode(null);
                user.setResetCodeCreationTime(null);
                userRepository.save(user);
                return ResponseEntity.ok("Password updated successfully!");
            }
        }
        return ResponseEntity.status(400).body("Error: Invalid password reset code!");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (code.equals(user.getVerificationCode())) {
                if (user.getOtpCreationTime() != null && LocalDateTime.now().isAfter(user.getOtpCreationTime().plusMinutes(3))) {
                    return ResponseEntity.status(400).body("Error: Verification code has expired!");
                }
                user.setVerified(true);
                user.setVerificationCode(null);
                user.setOtpCreationTime(null);
                userRepository.save(user);

                emailService.sendHtmlWelcomeEmail(user.getEmail(), user.getFullName());
                return ResponseEntity.ok("Verified successfully");
            }
        }
        return ResponseEntity.status(400).body("Error: Invalid verification code!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            if (!userOpt.get().isVerified()) {
                return ResponseEntity.status(403).body("NEEDS_VERIFICATION");
            }
            return ResponseEntity.ok(jwtUtil.generateToken(loginRequest.getEmail()));
        }
        return ResponseEntity.status(401).body("Error: Invalid email or password!");
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return userRepository.findByEmail(principal.getName()).map(ResponseEntity::ok).orElse(ResponseEntity.status(404).build());
    }
}