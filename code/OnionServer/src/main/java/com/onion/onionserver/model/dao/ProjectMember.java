package com.onion.onionserver.model.dao;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "project_member")
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String role;

    @Column
    private String status;

    @Column(name = "working_hour")
    private String workingHour;
}
