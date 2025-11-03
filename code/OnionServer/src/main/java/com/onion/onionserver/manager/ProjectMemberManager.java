package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.ProjectMember;
import com.onion.onionserver.model.dto.ProjectMemberAddDTO;
import com.onion.onionserver.repo.ProjectMemberRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMemberManager {

    private final ProjectMemberRepo repo;

    public ProjectMemberManager(ProjectMemberRepo repo) {
        this.repo = repo;
    }

    public ProjectMember addMember(Long projectId, ProjectMemberAddDTO dto) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(dto.getUserId());
        member.setRole(dto.getRole());
        member.setStatus(dto.getStatus());
        member.setWorkingHour(dto.getWorkingHour());
        return repo.save(member);
    }

    public List<ProjectMember> listMembers(Long projectId) {
        return repo.findByProjectId(projectId);
    }

    public void removeMember(Long memberId) {
        repo.deleteById(memberId);
    }
}
