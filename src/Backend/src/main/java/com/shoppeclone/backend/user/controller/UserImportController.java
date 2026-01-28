package com.shoppeclone.backend.user.controller;

import com.shoppeclone.backend.user.service.UserImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserImportController {

    private final UserImportService userImportService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(userImportService.importUsers(file));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> resetUsers() {
        userImportService.deleteAllUsers();
        return ResponseEntity.ok("All users deleted successfully!");
    }
}
