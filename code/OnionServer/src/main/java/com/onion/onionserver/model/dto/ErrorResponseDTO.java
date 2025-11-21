package com.onion.onionserver.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用于HTTP响应，表示对应的请求成功与否
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponseDTO {
    private String code;
    private String msg;

    public static ErrorResponseDTO createSuccess() {
        return new ErrorResponseDTO("success", null);
    }

    public static ErrorResponseDTO createFail(String msg) {
        return new ErrorResponseDTO("fail", msg);
    }
}
