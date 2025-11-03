package com.onion.onionserver.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectCreateDTO {
    private String name;
    private String description;
    private LocalDateTime expectedCompletion;
}
