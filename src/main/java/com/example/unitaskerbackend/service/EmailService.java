package com.example.unitaskerbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}") // Railway'den şifreyi çeker
    private String apiKey;

    private final String senderEmail = "freyaexe42@gmail.com";
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendVerificationCode(String toEmail, String code) {
        System.out.println("==================================================");
        System.out.println("API İLE GİDEN DOĞRULAMA KODU (" + toEmail + "): " + code);
        System.out.println("==================================================");

        String subject = "UniTasker Doğrulama Kodunuz";
        String htmlContent = "<div style='font-family: Arial; text-align: center; padding: 20px;'>"
                + "<h2>Sisteme giriş yapabilmek için doğrulama kodunuz:</h2>"
                + "<h1 style='color: #4f46e5; font-size: 36px;'>" + code + "</h1>"
                + "<p>Lütfen bu kodu kimseyle paylaşmayın.</p></div>";

        sendEmailViaBrevoApi(toEmail, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "UniTasker Şifre Sıfırlama Kodu 🔐";
        String htmlContent = "<h3>Şifrenizi sıfırlamak için kullanacağınız 6 haneli güvenlik kodunuz:</h3><h2>" + code + "</h2>";
        sendEmailViaBrevoApi(toEmail, subject, htmlContent);
    }

    @Async
    public void sendHtmlWelcomeEmail(String toEmail, String userName) {
        String subject = "UniTasker'a Hoş Geldiniz! 🚀";
        String htmlContent = "<div style='text-align: center; font-family: Arial;'>"
                + "<h2 style='color: #4f46e5;'>Hoş Geldiniz, " + userName + "!</h2>"
                + "<p>Hesabınız başarıyla doğrulandı. Çalışma alanınıza gidebilirsiniz.</p></div>";
        sendEmailViaBrevoApi(toEmail, subject, htmlContent);
    }

    // İŞTE SİHİRLİ API METODU (Railway Duvarını Delip Geçen Kod)
    private void sendEmailViaBrevoApi(String toEmail, String subject, String htmlContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", "UniTasker");
        sender.put("email", senderEmail);
        body.put("sender", sender);

        Map<String, String> to = new HashMap<>();
        to.put("email", toEmail);
        body.put("to", List.of(to));

        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            System.out.println("✅ E-POSTA API İLE BAŞARIYLA FIRLATILDI! Durum: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("❌ API Mail Gönderim Hatası: " + e.getMessage());
        }
    }
}