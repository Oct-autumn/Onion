package com.onion.onionserver.model.dto;

import lombok.Data;

@Data
public class ProjectMemberAddDTO {
    private Long userId;
    private String role;
    private String status;
    private String workingHour;
}
