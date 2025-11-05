package com.onion.onionserver.model.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "requirement")
public class Requirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    //TODO: foreign key?
    @Column(name = "assigner_id", nullable = false)
    private Long assignerId;

    @Column(name = "working_hour", nullable = false)
    private Float workingHour;

    //TODO: 只有有限种取值？要用什么类型
    @Column(name = "status", nullable = false)
    private String status;

    //TODO: foreign key?
    @Column(name = "project_id", nullable = false)
    private Long projectId;
}
