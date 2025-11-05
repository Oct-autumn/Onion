package com.onion.onionserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onion.onionserver.repo.RequirementRepo;

@RestController
public class TaskController {

    @Autowired
    RequirementRepo requirementRepo;

    @GetMapping("/kanban/tasks")
    public ResponseEntity<?> getTasks(@RequestParam long projectId) {
        return ResponseEntity.ok(requirementRepo.findAllByProjectId(projectId));
    }
}
