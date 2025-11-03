package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.model.dto.ProjectCreateDTO;
import com.onion.onionserver.repo.ProjectRepo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ProjectManager {
    private final ProjectRepo projectRepo;

    public ProjectManager(ProjectRepo projectRepo) {
        this.projectRepo = projectRepo;
    }

    public Project createProject(ProjectCreateDTO dto, Long ownerId) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setExpectedCompletion(dto.getExpectedCompletion());
        project.setOwnerId(ownerId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        return projectRepo.save(project);
    }

    public List<Project> listProjects() {
        return projectRepo.findAll();
    }

    public Project getProject(Long id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}
