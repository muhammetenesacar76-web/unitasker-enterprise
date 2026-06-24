package com.example.unitaskerbackend.controller;

import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private UserRepository userRepository;

    // ImgBB Bulut Sunucusu API Anahtarı
    private final String IMGBB_API_KEY = "a91b5a9c6add2f905c0ad15d060d67e1";

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Error: Unauthorized");

        try {
            // 1. Resmi Base64 formatına (Metne) çeviriyoruz (Buluta yollamak için en güvenli ve hızlı yol)
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

            // 2. ImgBB Sunucusuna fırlatmak için paketi hazırlıyoruz
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            String url = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;

            // 3. Paketi fırlat ve cevabı bekle
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            // 4. ImgBB bize resmin ASLA SİLİNMEYECEK kalıcı linkini veriyor!
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                // İşte ImgBB'nin bize verdiği o mükemmel kalıcı link
                String imageUrl = (String) data.get("url");

                // 5. Bu kalıcı linki Veritabanına (MariaDB) kaydet
                Optional<User> userOpt = userRepository.findByEmail(principal.getName());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setAvatarUrl(imageUrl);
                    userRepository.save(user);

                    return ResponseEntity.ok(imageUrl);
                } else {
                    return ResponseEntity.status(404).body("User not found in DB");
                }
            }

            return ResponseEntity.status(500).body("Bulut sunucusu resmi kabul etmedi.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Bulut yükleme hatası: " + e.getMessage());
        }
    }
}