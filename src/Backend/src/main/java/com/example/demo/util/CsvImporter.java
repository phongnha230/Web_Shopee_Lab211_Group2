package com.example.demo.util;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvImporter {

    @Autowired
    private UserRepository userRepository;

    /**
     * Import dữ liệu từ file CSV vào MongoDB
     * 
     * @param csvFilePath Đường dẫn tới file CSV
     * @return Số lượng bản ghi đã import thành công
     */
    public int importUsersFromCsv(String csvFilePath) {
        List<User> users = new ArrayList<>();
        int lineCount = 0;
        int successCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                lineCount++;

                // Bỏ qua dòng header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Parse CSV line
                String[] values = parseCsvLine(line);

                if (values.length >= 5) {
                    User user = new User();
                    user.setUsername(values[0].trim());
                    user.setEmail(values[1].trim());
                    user.setPassword(values[2].trim());
                    user.setFullName(values[3].trim());
                    user.setPhone(values[4].trim());

                    users.add(user);

                    // Batch insert mỗi 1000 bản ghi để tối ưu hiệu suất
                    if (users.size() >= 1000) {
                        userRepository.saveAll(users);
                        successCount += users.size();
                        System.out.println("Đã import " + successCount + " bản ghi...");
                        users.clear();
                    }
                }
            }

            // Insert các bản ghi còn lại
            if (!users.isEmpty()) {
                userRepository.saveAll(users);
                successCount += users.size();
            }

            System.out.println("✅ Hoàn thành! Đã import " + successCount + " bản ghi từ " + (lineCount - 1) + " dòng.");

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file CSV: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi import dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }

        return successCount;
    }

    /**
     * Parse một dòng CSV, xử lý trường hợp có dấu phẩy trong giá trị
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        result.add(currentValue.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Xóa tất cả users trong database (sử dụng cẩn thận!)
     */
    public void clearAllUsers() {
        userRepository.deleteAll();
        System.out.println("✅ Đã xóa tất cả users trong database.");
    }

    /**
     * Đếm số lượng users trong database
     */
    public long countUsers() {
        return userRepository.count();
    }
}
