package com.onion.onionserver.controller;

import com.onion.onionserver.manager.ProjectManager;
import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.model.dto.ProjectCreateDTO;
import com.onion.onionserver.model.dto.ProjectResponseDTO;
import com.onion.onionserver.util.JwtTools;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectManager projectManager;
    private final JwtTools jwtTools;

    public ProjectController(ProjectManager projectManager, JwtTools jwtTools) {
        this.projectManager = projectManager;
        this.jwtTools = jwtTools;
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> create(@RequestHeader("Authorization") String authorization,
                                                     @RequestBody ProjectCreateDTO dto) {
        Long ownerId = extractUserId(authorization);
        Project saved = projectManager.createProject(dto, ownerId);

        ProjectResponseDTO resp = new ProjectResponseDTO();
        resp.setProjectId(saved.getId());
        resp.setName(saved.getName());
        resp.setDescription(saved.getDescription());
        resp.setExpectedCompletion(saved.getExpectedCompletion());
        resp.setOwnerId(saved.getOwnerId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/list")
    public ResponseEntity<List<ProjectResponseDTO>> list() {
        List<ProjectResponseDTO> resp = projectManager.listProjects().stream().map(p -> {
            ProjectResponseDTO dto = new ProjectResponseDTO();
            dto.setProjectId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setExpectedCompletion(p.getExpectedCompletion());
            dto.setOwnerId(p.getOwnerId());
            return dto;
        }).toList();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<ProjectResponseDTO> info(@PathVariable Long id) {
        Project p = projectManager.getProject(id);
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setProjectId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setExpectedCompletion(p.getExpectedCompletion());
        dto.setOwnerId(p.getOwnerId());
        return ResponseEntity.ok(dto);
    }

    // ---- helpers ----

    private Long extractUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing or invalid");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        Claims claims = jwtTools.verifyToken(token);
        if (claims == null) {
            throw new RuntimeException("Invalid or expired token");
        }
        return JwtTools.getUserIdFromClaims(claims);
    }
}
