package com.onion.onionserver.repo;

import com.onion.onionserver.model.dao.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepo extends JpaRepository<Project, Long> {
    
    // 根据用户id 查询项目列表
    public List<Project> findByOwnerId(Integer ownerId);
}
