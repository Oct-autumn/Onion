package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.ProjectMember;
import com.onion.onionserver.model.dto.ProjectMemberAddDTO;
import com.onion.onionserver.repo.ProjectMemberRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProjectMemberManager {

    private final ProjectMemberRepo repo;

    public ProjectMemberManager(ProjectMemberRepo repo) {
        this.repo = repo;
    }

    // 添加成员
    public ProjectMember addMember(Long projectId, ProjectMemberAddDTO dto) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(dto.getUserId());
        member.setName(dto.getName());          // ✅ 存储冗余的名字
        member.setRole(dto.getRole());
        member.setStatus(dto.getStatus());
        member.setWorkingHour(dto.getWorkingHour());
        return repo.save(member);
    }

    // 查询成员列表
    public List<ProjectMember> listMembers(Long projectId) {
        return repo.findByProjectId(projectId);
    }

    // 删除成员（按 projectId + userId）
    public void removeMember(Long projectId, Long userId) {
        repo.deleteByProjectIdAndUserId(projectId, userId);
    }

    // 可选：查找单个成员
    public Optional<ProjectMember> findMember(Long projectId, Long userId) {
        return repo.findByProjectIdAndUserId(projectId, userId);
    }
    
    public ProjectMember updateMember(Long projectId, Long userId, ProjectMemberAddDTO dto) {
        
        ProjectMember member = repo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // 更新字段
        //member.setName(dto.getName());
        //member.setRole(dto.getRole());
        member.setStatus(dto.getStatus());
        member.setWorkingHour(dto.getWorkingHour());
        
        return repo.save(member);
    }
}
