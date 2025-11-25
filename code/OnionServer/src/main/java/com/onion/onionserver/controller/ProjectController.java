package com.onion.onionserver.controller;

import com.onion.onionserver.manager.ProjectManager;
import com.onion.onionserver.model.dao.Project;
import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dto.ProjectCreateDTO;
import com.onion.onionserver.model.dto.ProjectResponseDTO;
import com.onion.onionserver.model.dto.ProjectUpdateDto;
import com.onion.onionserver.model.enums.ProjectStatus;
import com.onion.onionserver.util.JwtTools;
import io.jsonwebtoken.Claims;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/project")
@CrossOrigin
public class ProjectController {

    private final ProjectManager projectManager;
    private final JwtTools jwtTools;

    public ProjectController(ProjectManager projectManager, JwtTools jwtTools) {
        this.projectManager = projectManager;
        this.jwtTools = jwtTools;
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> create(@AuthenticationPrincipal User authUser,
                                                     @RequestBody ProjectCreateDTO dto) {
        Integer ownerId = authUser.getId();
        Project saved = projectManager.createProject(dto, ownerId);

        ProjectResponseDTO resp = new ProjectResponseDTO();
        resp.setProjectId(saved.getId());
        resp.setName(saved.getName());
        resp.setDescription(saved.getDescription());
        resp.setExpectedCompletion(saved.getExpectedCompletion());
        resp.setOwnerId(saved.getOwnerId());
        resp.setStatus(saved.getStatus());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProjectResponseDTO>> list(@AuthenticationPrincipal User authUser) {
        Integer userId = authUser.getId();
        List<ProjectResponseDTO> resp = projectManager.projectListByOwnerId(userId).stream().map(p -> {
            ProjectResponseDTO dto = new ProjectResponseDTO();
            dto.setProjectId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setExpectedCompletion(p.getExpectedCompletion());
            dto.setOwnerId(p.getOwnerId());
            dto.setUserName(authUser.getUsername());
            dto.setCreateTime(p.getCreatedAt().format(DateTimeFormatter.ISO_DATE));
            dto.setStatus(p.getStatus());
            return dto;
        }).toList();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<ProjectResponseDTO> info(@PathVariable Long id) {
        Project p = projectManager.getProject(id);
        return ResponseEntity.ok(buidlResp(p));
    }
    
    @PutMapping("/update")
    public ResponseEntity update(@RequestBody ProjectUpdateDto projectUpdateDto) {
        Project p = projectManager.getProject(projectUpdateDto.getProjectId());
        ProjectStatus projectStatus =  projectUpdateDto.getStatus();
        p.setStatus(projectStatus);
        
        return ResponseEntity.ok(buidlResp(p));
    }
    
    private ProjectResponseDTO buidlResp (Project p) {
        Project saved = projectManager.updateProject(p);
        ProjectResponseDTO resp = new ProjectResponseDTO();
        resp.setProjectId(saved.getId());
        resp.setName(saved.getName());
        resp.setDescription(saved.getDescription());
        resp.setExpectedCompletion(saved.getExpectedCompletion());
        resp.setOwnerId(saved.getOwnerId());
        resp.setStatus(saved.getStatus());
        return  resp;
    }

    // ---- helpers ----

    private Integer extractUserId(String authorization) {
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
