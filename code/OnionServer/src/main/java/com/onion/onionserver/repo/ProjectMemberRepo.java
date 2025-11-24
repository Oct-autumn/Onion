package com.onion.onionserver.repo;

import com.onion.onionserver.model.dao.ProjectMember;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepo extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    
    @Transactional
    void deleteByProjectIdAndUserId(Long projectId, Long userId);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE ProjectMember p SET p.status  = :status WHERE p.workingHour = :w")
    int updateUserName(@Param("userId") Long userId, @Param("projectId") String projectId);
}
