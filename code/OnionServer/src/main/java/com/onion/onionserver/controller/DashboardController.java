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

import com.onion.onionserver.model.dao.Requirement;
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
        List<Requirement> list = requirementRepo.findAllByProjectIdAndAssignerId(projectId, userId);
        Map<String, Long> result = countTasksByStatus(list, userContributionFields);
        return ResponseEntity.ok(result);
    }

    private static final List<String> projectCompletionRateFields = Arrays.asList(new String[] {
            "new", "process", "test", "solved", "closed"
    });

    @GetMapping("/dashboard/completion_rate")
    public ResponseEntity<?> getProjectCompletionRate(
            @RequestParam("project_id") long projectId)
    {
        List<Requirement> list = requirementRepo.findAllByProjectId(projectId);
        Map<String, Long> filtered = countTasksByStatus(list, projectCompletionRateFields);
        Map<String, Long> result = new HashMap<>();
        filtered.forEach((k, v) -> {
            result.put(k+"_num", v);
        });
        return ResponseEntity.ok(result);
    }

    private static Map<String, Long> countTasksByStatus(List<Requirement> tasks, Iterable<String> allowedStatus) {
        Map<String, Long> result = new HashMap<>();
        for (String field : allowedStatus) {
            result.put(field, 0L);
        }
        for (Requirement requirement : tasks) {
            result.computeIfPresent(requirement.getStatus(), (k, v) -> v+1);
        }
        return result;
    }
}
