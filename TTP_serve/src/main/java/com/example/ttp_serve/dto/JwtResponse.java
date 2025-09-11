package com.example.ttp_serve.dto;

import com.example.ttp_serve.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String realName;
    private UserType userType;
    private Long campusId;

    public JwtResponse(String token, Long id, String username, String realName, UserType userType) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.userType = userType;
    }

    public JwtResponse(String token, Long id, String username, String realName, UserType userType, Long campusId) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.userType = userType;
        this.campusId = campusId;
    }
}