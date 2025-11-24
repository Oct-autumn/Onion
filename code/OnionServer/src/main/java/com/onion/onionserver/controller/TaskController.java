package com.onion.onionserver.controller;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dto.RequirementUpdateDTO;
import com.onion.onionserver.repo.UserRepo;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onion.onionserver.model.dao.Requirement;
import com.onion.onionserver.model.dto.ErrorResponseDTO;
import com.onion.onionserver.model.dto.RequirementCreateDTO;
import com.onion.onionserver.model.dto.RequirementStatusDTO;
import com.onion.onionserver.repo.RequirementRepo;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class TaskController {
    Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    RequirementRepo requirementRepo;

    @Autowired
    UserRepo userRepo;
    
    @GetMapping("/kanban/tasks")
    public ResponseEntity<?> getTasks(@RequestParam long projectId) {
        //List<Requirement> requirements = requirementRepo.findAllByProjectId(projectId);
        //ArrayList<RequirementCreateDTO> requirementCreateDTOS = new ArrayList<>();
        //
        //for (Requirement requirement : requirements) {
        //    RequirementCreateDTO requirementCreateDTO = new RequirementCreateDTO();
        //}
        
        return ResponseEntity.ok(requirementRepo.findAllByProjectId(projectId));
    }

    @PostMapping("/kanban/tasks/add")
    public ResponseEntity<?> postTasks(@RequestBody RequirementCreateDTO dto) {
        logger.error("dto is " + dto);
        Requirement requirement = new Requirement();
        //TODO: 如果assigner为空，从登录数据获取用户id
        //Integer userId = userRepo.findByUsername(dto.getAssigneer()).getId();
        requirement.setAssignerId(Long.valueOf(dto.getAssigneer()));
        requirement.setDescription(dto.getDescription());
        requirement.setProjectId(dto.getProjectId());
        requirement.setStatus(dto.getStatus());
        requirement.setTitle(dto.getTitle());
        requirement.setWorkingHour(parseWorkingHourString(dto.getWorkingHour()));
    
        logger.error("requirement is " + requirement);
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
            return ResponseEntity.badRequest().body(ErrorResponseDTO.createFail("requirement id not found"));
        }
        requirement.setStatus(statusDTO.getStatus());
        requirementRepo.save(requirement);
        //TODO: 这个API返回什么？任务目前的status？还是"success"?
        return ResponseEntity.ok(statusDTO);
    }
    
    @PutMapping("/kanban/tasks/single/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable(name = "id") long requirementId,
            @RequestBody RequirementUpdateDTO statusDTO)
    {
        Requirement requirement = requirementRepo.findById(requirementId).orElse(null);
        if (requirement == null) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO.createFail("requirement id not found"));
        }
        requirement.setDescription(statusDTO.getDescription());
        requirement.setAssignerId(Long.valueOf(statusDTO.getAssigneer()));
        requirement.setWorkingHour(parseWorkingHourString(statusDTO.getWorkingHour()));
        requirement.setTitle(statusDTO.getTitle());
        requirement.setStatus(statusDTO.getStatus());
        
        requirementRepo.save(requirement);
        //TODO: 这个API返回什么？任务目前的status？还是"success"?
        return ResponseEntity.ok(statusDTO);
    }

    //TODO: 换个端点
    //TODO: 以及其他问题
    @DeleteMapping("/task/delete/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable(name = "id") long requirementId) {
        requirementRepo.deleteById(requirementId);
        return ResponseEntity.ok(ErrorResponseDTO.createSuccess());
    }

    private float parseWorkingHourString(String str) {
        if (str.endsWith("h") || str.endsWith("H")) {
            str = str.substring(0, str.length()-1).stripTrailing();
        }
        return Float.parseFloat(str);
    }
}
