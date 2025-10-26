package com.onion.onionserver.service;

import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.repo.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }
}
