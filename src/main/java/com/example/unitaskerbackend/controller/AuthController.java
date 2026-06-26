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
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;
    private final Map<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5; // Maksimum hatalı deneme hakkı

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
    public ResponseEntity<String> verifyOTP(@RequestBody VerifyRequest request) { // Sende request sınıfı farklı olabilir (örn: LoginRequest), e-posta ve kodu nereden alıyorsan ona uyarla

        String email = request.getEmail();

        // 1. KONTROL: Bu e-posta daha önce sınırı aşmış mı?
        int attempts = otpAttempts.getOrDefault(email, 0);
        if (attempts >= MAX_ATTEMPTS) {
            return ResponseEntity.status(429).body("Güvenlik İhlali: Çok fazla hatalı deneme yaptınız. Hesabınız geçici olarak kilitlendi!");
            // (Kurumsal sistemlerde burada 15 dakika bekletilir veya hesaba kilit vurulur)
        }

        // 2. KENDİ KODUN: Senin yazdığın o OTP doğrulama işlemleri burada çalışsın
        boolean isCodeValid = authService.verifyCode(email, request.getCode()); // Senin metot adın neyse o

        // 3. SONUÇ: Kod Yanlışsa
        if (!isCodeValid) {
            otpAttempts.put(email, attempts + 1); // Başarısız deneme sayısını 1 artır
            return ResponseEntity.status(400).body("Geçersiz veya süresi dolmuş kod! Kalan hakkınız: " + (MAX_ATTEMPTS - attempts - 1));
        }

        // 4. SONUÇ: Kod Doğruysa
        otpAttempts.remove(email); // Kişi başardıysa sabıka kaydını temizle!

        // ... senin token üretme veya başarılı yanıt dönme kodların ...
        return ResponseEntity.ok("Verified successfully!");
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