package com.onion.onionserver.model.dto;

import lombok.Data;

@Data
public class RequirementCreateDTO {
    private String title;
    private String description;
    private long assignerId;
    private String workingHour;
    private String status;
    private long projectId;
}
