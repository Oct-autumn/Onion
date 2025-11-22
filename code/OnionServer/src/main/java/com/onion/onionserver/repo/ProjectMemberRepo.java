package com.onion.onionserver.repo;

import com.onion.onionserver.model.dao.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
}
