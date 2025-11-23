package com.onion.onionserver.controller;

import com.onion.onionserver.manager.ProjectMemberManager;
import com.onion.onionserver.model.dao.ProjectMember;
import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dto.ProjectMemberAddDTO;
import com.onion.onionserver.model.dto.ProjectMemberResponseDTO;
import com.onion.onionserver.util.JwtTools;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;

@RestController
@RequestMapping("/project/info/{projectId}/team")
@CrossOrigin
public class ProjectMemberController {

    private final ProjectMemberManager manager;
    private final JwtTools jwtTools;

    public ProjectMemberController(ProjectMemberManager manager, JwtTools jwtTools) {
        this.manager = manager;
        this.jwtTools = jwtTools;
    }

    // 获取成员列表（分页）
    @GetMapping
    public ResponseEntity<?> list(@PathVariable Long projectId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        List<ProjectMember> all = manager.listMembers(projectId);
        int fromIndex = Math.max((page - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, all.size());
        List<ProjectMember> paged = all.subList(fromIndex, toIndex);

        List<ProjectMemberResponseDTO> members = paged.stream().map(m -> {
            ProjectMemberResponseDTO dto = new ProjectMemberResponseDTO();
            dto.setId(m.getId());
            dto.setUserId(m.getUserId());
            dto.setName(m.getName());
            dto.setStatus(m.getStatus());
            dto.setWorkingHour(m.getWorkingHour());
            dto.setRole(m.getRole());
            return dto;
        }).toList();

        return ResponseEntity.ok(new HashMap<>() {{
            put("members", members);
            put("total", all.size());
        }});
    }

    // 添加成员
    @PostMapping
    public ResponseEntity<ProjectMemberResponseDTO> add(@AuthenticationPrincipal User authUser,
                                                        @PathVariable Long projectId,
                                                        @RequestBody ProjectMemberAddDTO dto) {
        Integer operatorId = authUser.getId();
        // TODO: 可以在这里加权限校验，比如 operatorId 是否是项目 owner

        ProjectMember saved = manager.addMember(projectId, dto);
        ProjectMemberResponseDTO resp = new ProjectMemberResponseDTO();
        resp.setId(saved.getId());
        resp.setUserId(saved.getUserId());
        resp.setName(saved.getName());
        resp.setRole(saved.getRole());
        resp.setStatus(saved.getStatus());
        resp.setWorkingHour(saved.getWorkingHour());
        return ResponseEntity.ok(resp);
    }

    // 删除成员（注意这里的 memberId 实际是 userId）
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User authUser,
                                       @PathVariable Long projectId,
                                       @PathVariable Long memberId) {
        Integer operatorId = authUser.getId();
        // TODO: 权限校验
        manager.removeMember(projectId, memberId); // ✅ 改成按 projectId + userId 删除
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
