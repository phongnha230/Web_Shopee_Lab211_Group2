package com.shoppeclone.backend.user.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCsvRepresentation {

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "password")
    private String password;

    @CsvBindByName(column = "fullName")
    private String fullName;

    @CsvBindByName(column = "phone")
    private String phone;
}
