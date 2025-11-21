package com.onion.onionserver.controller;

import com.onion.onionserver.manager.ProjectMemberManager;
import com.onion.onionserver.model.dao.ProjectMember;
import com.onion.onionserver.model.dto.ProjectMemberAddDTO;
import com.onion.onionserver.model.dto.ProjectMemberResponseDTO;
import com.onion.onionserver.util.JwtTools;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project/{projectId}/team")
@CrossOrigin
public class ProjectMemberController {

    private final ProjectMemberManager manager;
    private final JwtTools jwtTools;

    public ProjectMemberController(ProjectMemberManager manager, JwtTools jwtTools) {
        this.manager = manager;
        this.jwtTools = jwtTools;
    }

    // 获取成员列表
    @GetMapping
    public ResponseEntity<List<ProjectMemberResponseDTO>> list(@PathVariable Long projectId) {
        List<ProjectMemberResponseDTO> resp = manager.listMembers(projectId).stream().map(m -> {
            ProjectMemberResponseDTO dto = new ProjectMemberResponseDTO();
            dto.setId(m.getId());
            dto.setUserId(m.getUserId());
            dto.setRole(m.getRole());
            dto.setStatus(m.getStatus());
            dto.setWorkingHour(m.getWorkingHour());
            return dto;
        }).toList();
        return ResponseEntity.ok(resp);
    }

    // 添加成员
    @PostMapping
    public ResponseEntity<ProjectMemberResponseDTO> add(@RequestHeader("Authorization") String authorization,
                                                        @PathVariable Long projectId,
                                                        @RequestBody ProjectMemberAddDTO dto) {
        Integer operatorId = extractUserId(authorization);
        // TODO: 可以在这里加权限校验，比如 operatorId 是否是项目 owner

        ProjectMember saved = manager.addMember(projectId, dto);
        ProjectMemberResponseDTO resp = new ProjectMemberResponseDTO();
        resp.setId(saved.getId());
        resp.setUserId(saved.getUserId());
        resp.setRole(saved.getRole());
        resp.setStatus(saved.getStatus());
        resp.setWorkingHour(saved.getWorkingHour());
        return ResponseEntity.ok(resp);
    }

    // 删除成员
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String authorization,
                                       @PathVariable Long projectId,
                                       @PathVariable Long memberId) {
        Integer operatorId = extractUserId(authorization);
        // TODO: 权限校验
        manager.removeMember(memberId);
        return ResponseEntity.ok().build();
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
