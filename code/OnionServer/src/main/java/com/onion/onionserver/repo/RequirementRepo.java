package com.onion.onionserver.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onion.onionserver.model.dao.Requirement;

public interface RequirementRepo extends JpaRepository<Requirement, Long> {
    List<Requirement> findAllByProjectId(long projectId);
}
