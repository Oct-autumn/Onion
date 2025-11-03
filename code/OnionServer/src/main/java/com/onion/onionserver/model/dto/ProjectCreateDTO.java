package com.onion.onionserver.model.dto;

import lombok.Data;

@Data
public class ProjectCreateDTO {
    private String name;
    private String description;
    private String expectedCompletion;
}
