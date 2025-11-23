package com.onion.onionserver.model.dto;

import lombok.Data;

@Data
public class RequirementUpdateDTO {
    private String title;
    private String description;
    private String assigneer;
    private String workingHour;
    private String status;
}
