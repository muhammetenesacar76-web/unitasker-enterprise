package com.example.unitaskerbackend.controller;

import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private UserRepository userRepository;

    // Resimlerin kaydedileceği ana klasör
    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Error: Unauthorized");

        try {
            // "uploads" klasörü yoksa otomatik olarak oluşturur
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Resmi aynı isimle kaydedersek çakışma olur, bu yüzden rastgele eşsiz bir isim (UUID) veriyoruz
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Dosyayı diske (bilgisayara) kaydet
            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Veritabanındaki kullanıcının profil resmi linkini güncelle
            Optional<User> userOpt = userRepository.findByEmail(principal.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Link formatı: /uploads/rastgeleisim.jpg
                String avatarUrl = "/uploads/" + newFileName;
                user.setAvatarUrl(avatarUrl);
                userRepository.save(user);

                return ResponseEntity.ok(avatarUrl);
            }
            return ResponseEntity.status(404).body("User not found");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }
}