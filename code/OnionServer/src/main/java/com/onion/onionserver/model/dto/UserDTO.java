package com.onion.onionserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

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
        private Integer userId;
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
        private Integer id;
        private int role; // 0: normal user, 1: admin
    }

    @Data
    public static class UpdateInfoRequest {
        private String username;
        private String email;
        private Integer role;
        private Integer user_id;
    }

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
        private Integer user_id;
    }

    @Data
    public static class GetInfoResponse {
        private List<UserInfo> users;
        private int total;

        @Data
        public static class UserInfo {
            private Integer id;
            private String username;
            private String email;
            private int role;

            public static UserInfo fromDao(com.onion.onionserver.model.dao.User user) {
                var ret = new UserInfo();
                ret.id = user.getId();
                ret.username = user.getUsername();
                ret.email = user.getEmail();
                ret.role = user.getRole();
                return ret;
            }
        }
    }
}
