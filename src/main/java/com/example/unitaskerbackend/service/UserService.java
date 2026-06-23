package com.example.unitaskerbackend.service;

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
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) return null;

        // Şimdilik en yüksek levele sahip kullanıcıyı döndürür, ileride kategori bazlı filtre eklenebilir.
        User expert = users.get(0);
        for (User u : users) {
            if (u.getLevel() > expert.getLevel()) {
                expert = u;
            }
        }
        return expert;
    }
}