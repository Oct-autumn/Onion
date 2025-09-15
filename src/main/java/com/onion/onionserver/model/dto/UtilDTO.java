package com.onion.onionserver.model.dto;

import lombok.Data;

public class UtilDTO {
    @Data
    public static class GenericResponse {
        private String message;
        private int statusCode;
    }
}
