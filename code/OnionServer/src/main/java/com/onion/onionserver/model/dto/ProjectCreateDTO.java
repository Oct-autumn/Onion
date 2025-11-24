package com.onion.onionserver.model.dto;

import com.onion.onionserver.model.enums.ProjectStatus;
import lombok.Data;

@Data
public class ProjectCreateDTO {
    private String name;
    private String description;
    private String expectedCompletion;
    private ProjectStatus status;
    private Integer ownerId;
}
