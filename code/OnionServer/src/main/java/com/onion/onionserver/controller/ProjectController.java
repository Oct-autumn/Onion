package com.onion.onionserver.controller;

import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.model.dto.ProjectCreateDTO;
import com.onion.onionserver.model.dto.ProjectResponseDTO;
import com.onion.onionserver.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // 创建项目
    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> create(@RequestBody ProjectCreateDTO dto) {
        Long ownerId = 1L; // TODO: 从 token 解析
        Project saved = projectService.createProject(dto, ownerId);

        ProjectResponseDTO resp = new ProjectResponseDTO();
        resp.setProjectId(saved.getId());
        resp.setName(saved.getName());
        resp.setDescription(saved.getDescription());
        resp.setExpectedCompletion(saved.getExpectedCompletion());
        resp.setOwnerId(saved.getOwnerId());

        return ResponseEntity.ok(resp);
    }

    // 查询项目列表
    @PostMapping("/list")
    public ResponseEntity<List<ProjectResponseDTO>> list() {
        List<ProjectResponseDTO> resp = projectService.listProjects().stream().map(p -> {
            ProjectResponseDTO dto = new ProjectResponseDTO();
            dto.setProjectId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setExpectedCompletion(p.getExpectedCompletion());
            dto.setOwnerId(p.getOwnerId());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    // 查询单个项目详情
    @GetMapping("/info/{id}")
    public ResponseEntity<ProjectResponseDTO> info(@PathVariable Long id) {
        Project p = projectService.getProject(id);
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setProjectId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setExpectedCompletion(p.getExpectedCompletion());
        dto.setOwnerId(p.getOwnerId());
        return ResponseEntity.ok(dto);
    }
}
