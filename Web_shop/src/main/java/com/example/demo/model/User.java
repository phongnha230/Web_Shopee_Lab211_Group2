package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;

    // Lombok sẽ tự động tạo getter, setter, toString, equals, hashCode
}
