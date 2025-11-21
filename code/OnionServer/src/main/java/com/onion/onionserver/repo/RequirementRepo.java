package com.onion.onionserver.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onion.onionserver.model.dao.Requirement;
import com.onion.onionserver.model.dto.RequirementStatusCountDTO;

public interface RequirementRepo extends JpaRepository<Requirement, Long> {
    List<Requirement> findAllByProjectId(long projectId);

    //TODO: 真的需要硬编码SQL吗？
    //TODO: duplicated code?
    @Query("SELECT status, COUNT(id) number FROM Requirement"
            + " WHERE projectId = :projectId"
            + " GROUP BY status")
    List<RequirementStatusCountDTO> countAllStatusByProjectId(long projectId);
    @Query("SELECT status, COUNT(id) number FROM Requirement"
            + " WHERE projectId = :projectId AND assignerId = :assignerId"
            + " GROUP BY status")
    List<RequirementStatusCountDTO> countAllStatusByProjectIdAndAssignerId(long projectId, long assignerId);
}
