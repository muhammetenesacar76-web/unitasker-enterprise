package com.example.unitaskerbackend.service;

import com.example.unitaskerbackend.model.Task;
import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addUser(User newUser) {
        userRepository.save(newUser);
    }

    public String updateUser(Long id, User updatedInfo) {
        User existingUser = userRepository.findById(id).orElse(null);
        if(existingUser != null) {
            if(updatedInfo.getFullName() != null) existingUser.setFullName(updatedInfo.getFullName());
            if(updatedInfo.getEmail() != null) existingUser.setEmail(updatedInfo.getEmail());
            userRepository.save(existingUser);
            return "User updated successfully.";
        }
        return "User not found.";
    }

    // Basit uzman tavsiye algoritması
    public User getCategoryExpert(String category) {
        List<User> allUsers = userRepository.findAll();
        User expertUser = null;
        int maxCompletedTasks = 0;

        for (User user : allUsers) {
            int completedCount = 0;

            // Kullanıcının görevleri boş değilse saymaya başla
            if (user.getTasks() != null) {
                for (Task task : user.getTasks()) {
                    // Sadece SEÇİLEN KATEGORİDEKİ ve durumu COMPLETED olanları say!
                    if (category.equals(task.getCategory()) && "COMPLETED".equals(task.getStatus())) {
                        completedCount++;
                    }
                }
            }

            // Eğer bu kullanıcının tamamladığı görev sayısı şu ana kadarki en yüksek sayıysa, uzman ilan et
            if (completedCount > maxCompletedTasks) {
                maxCompletedTasks = completedCount;
                expertUser = user;
            }
        }

        // Eğer o kategoride hiç kimse görev bitirmemişse null döner, ön yüz "veri yok" der.
        return expertUser;
    }
}