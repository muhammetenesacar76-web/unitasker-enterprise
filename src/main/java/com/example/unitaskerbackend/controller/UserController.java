package com.example.unitaskerbackend.controller;

import com.example.unitaskerbackend.model.User;
import com.example.unitaskerbackend.repository.UserRepository;
import com.example.unitaskerbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(@Valid @RequestBody User newUser) {
        userService.addUser(newUser);
        return ResponseEntity.ok("User " + newUser.getFullName() + " created successfully.");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody User updatedInfo) {
        String result = userService.updateUser(id, updatedInfo);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User profile and all associated tasks deleted successfully.");
    }

    @GetMapping("/recommendation")
    public User getCategoryExpert(@RequestParam String category) {
        return userService.getCategoryExpert(category);
    }
}