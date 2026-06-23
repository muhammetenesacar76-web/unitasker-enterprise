package com.example.unitaskerbackend.controller;

import com.example.unitaskerbackend.model.SubTask;
import com.example.unitaskerbackend.model.Task;
import com.example.unitaskerbackend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Canlı Bildirim Radyosu
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/users/{userId}/tasks")
    public List<Task> getUserTasks(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {

        if (status != null && !status.isEmpty()) {
            return taskService.getUserTasksByStatus(userId, status);
        }
        return taskService.getUserTasks(userId);
    }

    @PostMapping("/users/{userId}/tasks/add")
    public ResponseEntity<String> addTask(@PathVariable Long userId, @Valid @RequestBody Task newTask) {
        // 1. Görevi veritabanına kaydet
        String result = taskService.addTask(userId, newTask);

        // 2. YENİ: Başarıyla kaydedildiyse WebSocket ile canlı bildirim yolla
        try {
            // Sadece görevin atandığı kişinin (userId) frekansına mesaj fırlatıyoruz
            String message = "🔔 Yeni Görev: " + newTask.getTitle() + " panonuza eklendi!";
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, message);
        } catch (Exception e) {
            System.out.println("Canlı bildirim gönderilemedi: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/tasks/delete/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        String result = taskService.deleteTask(taskId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/tasks/update/{id}")
    public ResponseEntity<String> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        String result = taskService.updateTask(id, updatedTask);
        return ResponseEntity.ok(result);
    }

    // Alt Görev Ekleme Endpoint'i
    @PostMapping("/tasks/{taskId}/subtasks/add")
    public ResponseEntity<SubTask> addSubTask(@PathVariable Long taskId, @RequestBody SubTask subTask) {
        SubTask result = taskService.addSubTask(taskId, subTask);
        return ResponseEntity.ok(result);
    }

    // Alt Görevi Tamamlandı Olarak İşaretleme Endpoint'i
    @PutMapping("/tasks/subtasks/{subTaskId}/toggle")
    public ResponseEntity<String> toggleSubTask(@PathVariable Long subTaskId) {
        String result = taskService.toggleSubTask(subTaskId);
        return ResponseEntity.ok(result);
    }
}