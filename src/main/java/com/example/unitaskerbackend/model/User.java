package com.example.unitaskerbackend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty; // YENİ IMPORT
import java.util.List;
import java.time.LocalDateTime;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private boolean isVerified = false;
    private String verificationCode;
    private String resetCode;
    private java.time.LocalDateTime resetCodeCreationTime;
    // HATA DÜZELTİLDİ: @JsonIgnore kaldırıldı.
    // Bu sayede ön yüzden şifre alınabilir (WRITE) ama ön yüze şifre sızdırılmaz (READ).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String avatarUrl;

    private String role = "ROLE_USER"; // ROLE_USER veya ROLE_ADMIN
    private Integer xp = 0;
    private Integer level = 1;
    private LocalDateTime otpCreationTime;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    public User() {}

    // ... Mevcut tüm Getter ve Setter metodların aynen kalacak ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getXp() { return xp == null ? 0 : xp; }
    public void setXp(Integer xp) { this.xp = xp; }
    public Integer getLevel() { return level == null ? 1 : level; }
    public void setLevel(Integer level) { this.level = level; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public LocalDateTime getOtpCreationTime() { return otpCreationTime; }
    public void setOtpCreationTime(LocalDateTime otpCreationTime) { this.otpCreationTime = otpCreationTime; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }
    public java.time.LocalDateTime getResetCodeCreationTime() { return resetCodeCreationTime; }
    public void setResetCodeCreationTime(java.time.LocalDateTime resetCodeCreationTime) { this.resetCodeCreationTime = resetCodeCreationTime; }
    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }
}