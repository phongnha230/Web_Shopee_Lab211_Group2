package com.example.demo.controller;

import com.example.demo.util.CsvImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
public class ImportController {

    @Autowired
    private CsvImporter csvImporter;

    /**
     * Import users từ file CSV được upload
     */
    @PostMapping("/users/upload")
    public ResponseEntity<?> uploadAndImportCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File không được để trống!");
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Chỉ chấp nhận file CSV!");
        }

        try {
            // Lưu file tạm thời
            File tempFile = File.createTempFile("users_", ".csv");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            // Import dữ liệu
            int importedCount = csvImporter.importUsersFromCsv(tempFile.getAbsolutePath());

            // Xóa file tạm
            tempFile.delete();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Import thành công!");
            response.put("importedCount", importedCount);
            response.put("totalUsers", csvImporter.countUsers());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Lỗi khi xử lý file: " + e.getMessage());
        }
    }

    /**
     * Import users từ file CSV có sẵn trên server
     */
    @PostMapping("/users/from-file")
    public ResponseEntity<?> importFromServerFile(@RequestParam String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return ResponseEntity.badRequest().body("File không tồn tại: " + filePath);
        }

        int importedCount = csvImporter.importUsersFromCsv(filePath);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Import thành công!");
        response.put("importedCount", importedCount);
        response.put("totalUsers", csvImporter.countUsers());

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa tất cả users (cẩn thận!)
     */
    @DeleteMapping("/users/clear")
    public ResponseEntity<?> clearAllUsers() {
        long countBefore = csvImporter.countUsers();
        csvImporter.clearAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa tất cả users!");
        response.put("deletedCount", countBefore);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin số lượng users
     */
    @GetMapping("/users/count")
    public ResponseEntity<?> getUserCount() {
        long count = csvImporter.countUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", count);

        return ResponseEntity.ok(response);
    }
}
