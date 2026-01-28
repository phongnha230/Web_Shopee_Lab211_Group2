package com.shoppeclone.backend.user.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.shoppeclone.backend.auth.model.Role;
import com.shoppeclone.backend.auth.model.User;
import com.shoppeclone.backend.auth.repository.RoleRepository;
import com.shoppeclone.backend.auth.repository.UserRepository;
import com.shoppeclone.backend.user.dto.UserCsvRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserImportService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> importUsers(MultipartFile file) throws IOException {
        Set<User> usersToSave = new HashSet<>();
        List<String> errors = new ArrayList<>();
        Map<String, String> passwordCache = new HashMap<>(); // Cache for performance
        int successCount = 0;
        int errorCount = 0;

        // Default Role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Pre-fetch existing emails for performance
        Set<String> existingEmails = userRepository.findAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<UserCsvRepresentation> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(UserCsvRepresentation.class);

            CsvToBean<UserCsvRepresentation> csvToBean = new CsvToBeanBuilder<UserCsvRepresentation>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<UserCsvRepresentation> csvUserIterator = csvToBean.iterator();

            int line = 1;
            while (csvUserIterator.hasNext()) {
                line++;
                try {
                    UserCsvRepresentation csvUser = csvUserIterator.next();

                    // 1. Validate Required Fields
                    if (csvUser.getEmail() == null || csvUser.getEmail().isEmpty()) {
                        errors.add("Line " + line + ": Missing Email");
                        errorCount++;
                        continue;
                    }
                    if (csvUser.getFullName() == null || csvUser.getFullName().isEmpty()) {
                        errors.add("Line " + line + ": Missing FullName");
                        errorCount++;
                        continue;
                    }

                    // 2. Validate Email Format (Simple Regex)
                    if (!csvUser.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        errors.add("Line " + line + ": Invalid Email Format (" + csvUser.getEmail() + ")");
                        errorCount++;
                        continue;
                    }

                    // 3. Check Duplicate in DB
                    if (existingEmails.contains(csvUser.getEmail())) {
                        errors.add("Line " + line + ": Email already exists (" + csvUser.getEmail() + ")");
                        errorCount++;
                        continue;
                    }

                    // 4. Check Duplicate in Current Batch
                    boolean isDuplicateInBatch = usersToSave.stream()
                            .anyMatch(u -> u.getEmail().equals(csvUser.getEmail()));
                    if (isDuplicateInBatch) {
                        errors.add("Line " + line + ": Duplicate Email in CSV (" + csvUser.getEmail() + ")");
                        errorCount++;
                        continue;
                    }

                    // Map to Entity
                    User user = new User();
                    user.setEmail(csvUser.getEmail());

                    // Optimize Password Encoding (Cache)
                    String rawPassword = csvUser.getPassword();
                    if (!passwordCache.containsKey(rawPassword)) {
                        passwordCache.put(rawPassword, passwordEncoder.encode(rawPassword));
                    }
                    user.setPassword(passwordCache.get(rawPassword));

                    user.setFullName(csvUser.getFullName());
                    user.setPhone(csvUser.getPhone());
                    user.setRoles(roles);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    user.setEmailVerified(true); // Auto verify imported users

                    usersToSave.add(user);
                    successCount++;

                } catch (Exception e) {
                    errors.add("Line " + line + ": " + e.getMessage());
                    errorCount++;
                }
            }
        }

        // Batch Save
        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalProcessed", successCount + errorCount);
        response.put("successCount", successCount);
        response.put("errorCount", errorCount);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }

        return response;
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
