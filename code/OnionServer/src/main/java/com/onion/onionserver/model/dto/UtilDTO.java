package com.onion.onionserver.model.dto;

import lombok.Data;

public class UtilDTO {
    @Data
    public static class ErrorResponse {
        private int code;
        private String msg;

        public ErrorResponse(int code) {
            this.code = code;
            this.msg = "";
        }

        public ErrorResponse(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
