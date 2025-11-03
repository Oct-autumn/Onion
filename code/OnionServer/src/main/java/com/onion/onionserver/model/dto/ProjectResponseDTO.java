package com.onion.onionserver.model.dto;

import lombok.Data;

@Data
public class ProjectResponseDTO {
    private Long projectId;
    private String name;
    private String description;
    private String expectedCompletion;
    private Long ownerId;
}
