package com.example.unitaskerbackend.service;

import com.example.unitaskerbackend.model.AuditLog;
import com.example.unitaskerbackend.model.SubTask;
import com.example.unitaskerbackend.model.Task;
import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.AuditLogRepository;
import com.example.unitaskerbackend.repository.SubTaskRepository;
import com.example.unitaskerbackend.repository.TaskRepository;
import com.example.unitaskerbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SubTaskRepository subTaskRepository;

    public List<Task> getUserTasks(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getUserTasksByStatus(Long userId, String status) {
        return taskRepository.findByUserIdAndStatus(userId, status);
    }

    public String addTask(Long userId, Task newTask) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "User not found!";

        newTask.setUser(user);
        Task savedTask = taskRepository.save(newTask);

        // YENİ: Görev oluşturulduğunda logla
        auditLogRepository.save(new AuditLog("Task created in " + savedTask.getCategory(), savedTask));

        return "Task added successfully.";
    }

    public String deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
        return "Task deleted successfully.";
    }

    public String updateTask(Long taskId, Task updatedInfo) {
        Task existingTask = taskRepository.findById(taskId).orElse(null);
        if (existingTask == null) return "Task not found!";

        boolean previouslyCompleted = "COMPLETED".equals(existingTask.getStatus());
        boolean nowCompleted = "COMPLETED".equals(updatedInfo.getStatus());

        // Edit modundan gelen güncellemeler
        if (updatedInfo.getTitle() != null && !updatedInfo.getTitle().isEmpty()) {
            existingTask.setTitle(updatedInfo.getTitle());
            existingTask.setDescription(updatedInfo.getDescription());
            existingTask.setCategory(updatedInfo.getCategory());
            existingTask.setPriority(updatedInfo.getPriority());
            existingTask.setDueDate(updatedInfo.getDueDate());

            // YENİ: Düzenleme yapıldığında logla
            auditLogRepository.save(new AuditLog("Task details updated", existingTask));
        }

        // Sürükle Bırak (Kanban) güncellemeleri
        if (updatedInfo.getStatus() != null && !updatedInfo.getStatus().equals(existingTask.getStatus())) {
            existingTask.setStatus(updatedInfo.getStatus());

            // YENİ: Sürükle bırak yapıldığında yeni durumu logla
            auditLogRepository.save(new AuditLog("Moved to " + updatedInfo.getStatus(), existingTask));
        }

        taskRepository.save(existingTask);

        // XP ve Level Sistemi
        if (existingTask.getUser() != null) {
            User u = existingTask.getUser();
            int xpValue = "HIGH".equals(existingTask.getPriority()) ? 50 : ("MEDIUM".equals(existingTask.getPriority()) ? 25 : 10);

            if (!previouslyCompleted && nowCompleted) {
                u.setXp(u.getXp() + xpValue);
                if (u.getXp() >= u.getLevel() * 100) {
                    u.setXp(u.getXp() - (u.getLevel() * 100));
                    u.setLevel(u.getLevel() + 1);
                }
            } else if (previouslyCompleted && !nowCompleted) {
                u.setXp(u.getXp() - xpValue);
                if (u.getXp() < 0 && u.getLevel() > 1) {
                    u.setLevel(u.getLevel() - 1);
                    u.setXp((u.getLevel() * 100) + u.getXp());
                } else if (u.getXp() < 0) { u.setXp(0); }
            }
            userRepository.save(u);
        }
        return "Task updated successfully!";
    }

    // YENİ: Alt Görev (Checklist) Ekleme
    public SubTask addSubTask(Long taskId, SubTask subTask) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        subTask.setTask(task);
        SubTask saved = subTaskRepository.save(subTask);
        auditLogRepository.save(new AuditLog("Added subtask: " + subTask.getTitle(), task));
        return saved;
    }

    // YENİ: Alt Görev Tamamlama/Geri Alma (Toggle)
    public String toggleSubTask(Long subTaskId) {
        SubTask subTask = subTaskRepository.findById(subTaskId).orElseThrow(() -> new RuntimeException("SubTask not found"));
        subTask.setCompleted(!subTask.isCompleted());
        subTaskRepository.save(subTask);

        String status = subTask.isCompleted() ? "Completed" : "Unchecked";
        auditLogRepository.save(new AuditLog(status + " subtask: " + subTask.getTitle(), subTask.getTask()));
        return "SubTask toggled";
    }
}