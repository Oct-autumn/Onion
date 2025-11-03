package com.onion.onionserver.service;

import com.onion.onionserver.manager.ProjectManager;
import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.model.dto.ProjectCreateDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectManager projectManager;

    public ProjectService(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public Project createProject(ProjectCreateDTO dto, Long ownerId) {
        return projectManager.createProject(dto, ownerId);
    }

    public List<Project> listProjects() {
        return projectManager.listProjects();
    }

    public Project getProject(Long id) {
        return projectManager.getProject(id);
    }
}
