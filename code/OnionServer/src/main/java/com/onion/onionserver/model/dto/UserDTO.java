package com.onion.onionserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * As a class project, there is no need to separate every DTO into different files.
 * But in a real project, please do so.
 */

public class UserDTO {
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private String authToken;
        private Long userId;
        private String username;
        private int role; // 0: normal user, 1: admin
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class RegisterResponse {
        private Long id;
        private int role; // 0: normal user, 1: admin
    }
}
