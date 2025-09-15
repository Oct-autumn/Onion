package com.onion.onionserver.controller;

import com.onion.onionserver.model.dto.User;
import com.onion.onionserver.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {
    // User-related endpoints would go here

    @Autowired
    public UserController(RedisService redis) {
        this.redis = redis;
    }

    /**
     * User Login
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody User.LoginRequest request) {

    }
}
