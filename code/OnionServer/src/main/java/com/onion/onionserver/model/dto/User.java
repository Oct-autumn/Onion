package com.onion.onionserver.model.dto;

import lombok.Data;

/*
 * As a class project, there is no need to separate every DTO into different files.
 * But in a real project, please do so.
 */

public class User {
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String username;
        private String email;
        private int role; // 0: normal user, 1: admin
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    @Data
    public static class RegisterResponse {
        private Long id;
        private String username;
        private String email;
        private int role; // 0: normal user, 1: admin
    }
}
