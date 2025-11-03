package com.onion.onionserver.service;

import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.model.dto.ProjectCreateDTO;
import com.onion.onionserver.model.repo.ProjectRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepo projectRepo;

    public ProjectService(ProjectRepo projectRepo) {
        this.projectRepo = projectRepo;
    }

    public Project createProject(ProjectCreateDTO dto, Long ownerId) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setExpectedCompletion(dto.getExpectedCompletion());
        project.setOwnerId(ownerId);
        project.setCreateAt(LocalDateTime.now());
        project.setUpdateAt(LocalDateTime.now());
        return projectRepo.save(project);
    }

    public List<Project> listProjects() {
        return projectRepo.findAll();
    }

    public Project getProject(Long id) {
        return projectRepo.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
    }
}
