package com.onion.onionserver.model.dto;

import com.onion.onionserver.model.enums.ProjectStatus;
import lombok.Data;

@Data
public class ProjectUpdateDto {
    private Long projectId;
    private ProjectStatus status;
}
