package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Thêm user mới
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        System.out.println("✅ User đã được tạo: " + createdUser);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // Lấy tất cả users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        System.out.println("📋 Danh sách users hiện tại: " + users);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Lấy user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            System.out.println("🔍 Tìm thấy user: " + user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            System.out.println("❌ Không tìm thấy user với ID: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Cập nhật user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        if (updatedUser != null) {
            System.out.println("🔄 User đã được cập nhật: " + updatedUser);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            System.out.println("❌ Không tìm thấy user để cập nhật với ID: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            System.out.println("🗑️ User đã được xóa với ID: " + id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            System.out.println("❌ Không tìm thấy user để xóa với ID: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
