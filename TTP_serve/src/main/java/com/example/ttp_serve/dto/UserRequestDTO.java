package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.Gender;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class UserRequestDTO {
    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 50)
    private String realName;

    private Gender gender;
    private Integer age;

    @Pattern(regexp = "^1[3-9]\\d{9}$")
    private String phone;

    @Email
    private String email;

    private String avatar;
    private UserType userType;
    private Long campusId;
    private UserStatus status;
}