package com.onion.onionserver.controller;

import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @PostMapping("/create")
    public Project createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    @GetMapping("/list")
    public List<Project> listProjects() {
        return projectService.listProjects();
    }
}
