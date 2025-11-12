package com.onion.onionserver.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onion.onionserver.model.dto.RequirementStatusCountDTO;
import com.onion.onionserver.repo.RequirementRepo;

@RestController
public class DashboardController {

    @Autowired
    private RequirementRepo requirementRepo;

    private static final List<String> userContributionFields = Arrays.asList(new String[] {
            "new", "inprocess", "code_review", "intest", "solved"
    });

    @GetMapping("/dashboard/contribution")
    public ResponseEntity<?> getUserContribution(
            @RequestParam("project_id") long projectId,
            @RequestParam("user_id") long userId)
    {
        List<RequirementStatusCountDTO> list = requirementRepo.countAllStatusByProjectIdAndAssignerId(projectId, userId);
        Map<String, Long> result = new HashMap<String, Long>();
        for (RequirementStatusCountDTO item : list) {
            result.put(item.getStatus(), item.getNumber());
        }
        for (String field : userContributionFields) {
            result.putIfAbsent(field, 0L);
        }
        return ResponseEntity.ok(result);
    }

    private static final List<String> projectCompletionRateFields = Arrays.asList(new String[] {
            "new", "process", "test", "solved", "closed"
    });

    @GetMapping("/dashboard/completion_rate")
    public ResponseEntity<?> getProjectCompletionRate(
            @RequestParam("project_id") long projectId)
    {
        final String SUFFIX = "_num";
        List<RequirementStatusCountDTO> list = requirementRepo.countAllStatusByProjectId(projectId);
        Map<String, Long> result = new HashMap<String, Long>();
        for (RequirementStatusCountDTO item : list) {
            result.put(item.getStatus()+SUFFIX, item.getNumber());
        }
        for (String field : projectCompletionRateFields) {
            result.putIfAbsent(field+SUFFIX, 0L);
        }
        return ResponseEntity.ok(result);
    }
}
