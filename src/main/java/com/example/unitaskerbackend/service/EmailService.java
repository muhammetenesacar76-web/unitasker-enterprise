package com.example.unitaskerbackend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Gönderici e-postasını buraya sabitliyoruz
    private final String senderEmail = "freyaexe42@gmail.com";

    // 1. ADIM: Sadece 6 haneli kodu içeren sade bir mail,
    @Async
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail); // KRİTİK EKLENTİ
            message.setTo(toEmail);
            message.setSubject("UniTasker Doğrulama Kodunuz");
            message.setText("Sisteme giriş yapabilmek için doğrulama kodunuz:\n\n" + code + "\n\nLütfen bu kodu kimseyle paylaşmayın.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("OTP E-postası gönderilemedi: " + e.getMessage());
        }
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail); // KRİTİK EKLENTİ
            message.setTo(toEmail);
            message.setSubject("UniTasker Şifre Sıfırlama Kodu 🔐");
            message.setText("Şifrenizi sıfırlamak için kullanacağınız 6 haneli güvenlik kodunuz:\n\n"
                    + code + "\n\nBu kod 5 dakika boyunca geçerlidir. İstek size ait değilse bu e-postayı dikkate almayınız.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Şifre sıfırlama maili gönderilemedi: " + e.getMessage());
        }
    }

    // 2. ADIM: Hesap onaylanınca giden ŞIK VE RESİMLİ HTML Mail
    @Async
    public void sendHtmlWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail); // KRİTİK EKLENTİ
            helper.setTo(toEmail);
            helper.setSubject("UniTasker'a Hoş Geldiniz! 🚀");

            // GÜNCELLENDİ: localhost linki yerine Railway Canlı Sunucu Linki eklendi!
            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>"
                    + "<div style='background: linear-gradient(135deg, #4f46e5, #7e22ce); padding: 40px 20px; text-align: center;'>"
                    + "<h1 style='color: white; margin: 0; font-size: 28px;'>🚀 UniTasker Workspace</h1>"
                    + "</div>"
                    + "<div style='padding: 30px; text-align: center; color: #1f2937;'>"
                    + "<h2 style='color: #4f46e5; margin-top: 0;'>Hoş Geldiniz, " + userName + "!</h2>"
                    + "<p style='font-size: 15px; color: #4b5563; line-height: 1.6;'>Hesabınız başarıyla doğrulandı. Artık UniTasker Enterprise üzerinde kendi görevlerinizi yönetebilir, XP kazanabilir ve performansınızı takip edebilirsiniz.</p>"
                    + "<div style='margin-top: 30px; margin-bottom: 20px;'>"
                    + "<a href='https://unitasker-enterprise-production.up.railway.app' style='background-color: #4f46e5; color: white; padding: 12px 25px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;'>Çalışma Alanına Git</a>"
                    + "</div>"
                    + "<p style='font-size: 12px; color: #9ca3af; margin-top: 30px;'>Bu e-posta otomatik olarak gönderilmiştir.</p>"
                    + "</div></div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("HTML E-postası gönderilemedi: " + e.getMessage());
        }
    }
}