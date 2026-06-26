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

    // ImgBB Bulut Sunucusu API Anahtarı (Kendi anahtarını buraya yaz)
    @Value("${imgbb.api.key}")
    private String imgbbApiKey;

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId,
            Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Error: Unauthorized");

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            String url = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                String imageUrl = (String) data.get("url");

                // GÜNCELLEME: Eğer ön yüzden userId gönderildiyse o kullanıcıyı bul, yoksa kendini bul.
                Optional<User> userOpt;
                if (userId != null) {
                    userOpt = userRepository.findById(userId);
                } else {
                    userOpt = userRepository.findByEmail(principal.getName());
                }

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setAvatarUrl(imageUrl);
                    userRepository.save(user);

                    return ResponseEntity.ok(imageUrl);
                } else {
                    return ResponseEntity.status(404).body("User not found");
                }
            }
            return ResponseEntity.status(500).body("Cloud server rejected image");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Cloud upload error: " + e.getMessage());
        }
    }
}