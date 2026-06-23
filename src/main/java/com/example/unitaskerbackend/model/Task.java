package com.example.unitaskerbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hata: Görev başlığı kesinlikle boş bırakılamaz!")
    private String title;

    private String description;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private String category;   // YENI: BILGISAYAR, SOSYAL, EGLENCE, KAS_GUCU

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Task() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    // ... Diğer mevcut özellikler (id, title, status, dueDate vb.) ...

    // --- YENİ EKLENEN KURUMSAL BAĞLANTILAR ---

    // 1. Bir görevin birden fazla alt görevi (checklist) olabilir
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubTask> subTasks;

    // 2. Bir görevin üzerinde yapılan tüm değişikliklerin (history) kaydı
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditLog> auditLogs;

    // --- YENİ ALANLARIN GETTER VE SETTER'LARI ---

    public List<SubTask> getSubTasks() { return subTasks; }
    public void setSubTasks(List<SubTask> subTasks) { this.subTasks = subTasks; }

    public List<AuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<AuditLog> auditLogs) { this.auditLogs = auditLogs; }
}