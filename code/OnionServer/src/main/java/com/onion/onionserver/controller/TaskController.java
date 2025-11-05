package com.onion.onionserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onion.onionserver.model.dao.Requirement;
import com.onion.onionserver.model.dto.RequirementCreateDTO;
import com.onion.onionserver.model.dto.RequirementStatusDTO;
import com.onion.onionserver.model.dto.UtilDTO;
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

        return ResponseEntity.ok(new RequirementStatusDTO(requirement.getStatus()));
    }

    @PutMapping("/kanban/tasks/{id}")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable(name = "id") long requirementId,
            @RequestBody RequirementStatusDTO statusDTO)
    {
        Requirement requirement = requirementRepo.findById(requirementId).orElse(null);
        if (requirement == null) {
            return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(1, "bad requirement id"));
        }
        requirement.setStatus(statusDTO.getStatus());
        requirementRepo.save(requirement);
        //TODO: 这个API返回什么？任务目前的status？还是"success"?
        return ResponseEntity.ok(statusDTO);
    }

    private float parseWorkingHourString(String str) {
        if (str.endsWith("h") || str.endsWith("H")) {
            str = str.substring(0, str.length()-1).stripTrailing();
        }
        return Float.parseFloat(str);
    }
}
