package com.onion.onionserver.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onion.onionserver.model.dao.Requirement;
import com.onion.onionserver.model.dto.RequirementCreateDTO;
import com.onion.onionserver.repo.RequirementRepo;

@RestController
public class TaskController {

    @Autowired
    RequirementRepo requirementRepo;

    @GetMapping("/kanban/tasks")
    public ResponseEntity<?> getTasks(@RequestParam long projectId) {
        return ResponseEntity.ok(requirementRepo.findAllByProjectId(projectId));
    }

    @PostMapping("/kanban/tasks")
    public ResponseEntity<?> postTasks(@RequestBody RequirementCreateDTO dto) {
        Requirement requirement = new Requirement();
        //TODO: 如果assigner为空，从登录数据获取用户id
        requirement.setAssignerId(dto.getAssignerId());
        requirement.setDescription(dto.getDescription());
        requirement.setProjectId(dto.getProjectId());
        requirement.setStatus(dto.getStatus());
        requirement.setTitle(dto.getTitle());
        requirement.setWorkingHour(parseWorkingHourString(dto.getWorkingHour()));
        requirementRepo.save(requirement);

        return ResponseEntity.ok(Map.of("status", dto.getStatus()));
    }

    private float parseWorkingHourString(String str) {
        if (str.endsWith("h") || str.endsWith("H")) {
            str = str.substring(0, str.length()-1).stripTrailing();
        }
        return Float.parseFloat(str);
    }
}
