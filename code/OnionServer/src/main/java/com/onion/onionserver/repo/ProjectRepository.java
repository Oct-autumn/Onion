package com.onion.onionserver.repo;

import com.onion.onionserver.model.dao.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
