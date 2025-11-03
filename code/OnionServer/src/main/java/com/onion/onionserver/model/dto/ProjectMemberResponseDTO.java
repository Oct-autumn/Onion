package com.onion.onionserver.model.dto;

import lombok.Data;

@Data
public class ProjectMemberResponseDTO {
    private Long id;
    private Long userId;
    private String role;
    private String status;
    private String workingHour;
}
