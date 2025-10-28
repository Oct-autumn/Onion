package com.onion.onionserver.controller;

import com.onion.onionserver.manager.AuthenticationManager;
import com.onion.onionserver.manager.UserManager;
import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dto.UserDTO;
import com.onion.onionserver.model.dto.UtilDTO;
import com.onion.onionserver.util.JwtTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    // User-related endpoints would go here

    private final Logger logger;

    private final UserManager userManager;
    private final AuthenticationManager authenticationManager;
    private final JwtTools jwtTools;

    @Autowired
    public UserController(UserManager userManager, AuthenticationManager authenticationManager, JwtTools jwtTools) {
        this.logger = LoggerFactory.getLogger(UserController.class);
        this.userManager = userManager;
        this.authenticationManager = authenticationManager;
        this.jwtTools = jwtTools;
    }

    /**
     * User Registration
     */
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody UserDTO.RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getUsername() == null) {
            return ResponseEntity.badRequest().body(
                    new UtilDTO.ErrorResponse(0)
            );
        }

        // Verify email format
        if (!request.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return ResponseEntity.badRequest().body(
                    new UtilDTO.ErrorResponse(2, "Invalid email format")
            );
        }

        // Verify username format
        if (request.getUsername().length() < 2 || request.getUsername().length() > 64) {
            return ResponseEntity.badRequest().body(
                    new UtilDTO.ErrorResponse(3, "Username must be between 2 and 64 characters")
            );
        }

        try {
            User user = userManager.createUser(request.getEmail(), request.getUsername());
            authenticationManager.createAuth(user.getId(), request.getPassword());
            return ResponseEntity.ok().body(
                    new UserDTO.RegisterResponse(user.getId(), user.getRole())
            );
        } catch (Exception e) {
            if (e.getMessage().equals("1")) {
                return ResponseEntity.badRequest().body(
                        new UtilDTO.ErrorResponse(1, "Email already registered")
                );
            } else {
                logger.warn("Unexpected error during registration", e);
                return ResponseEntity.internalServerError().body(
                        new UtilDTO.ErrorResponse(500, "Internal server error")
                );
            }
        }
    }

    /**
     * User Login
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody UserDTO.LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new UtilDTO.ErrorResponse(0)
            );
        }

        try {
            User user = authenticationManager.authenticate(request.getEmail(), request.getPassword());
            if (user == null) {
                return ResponseEntity.status(401).body(
                        new UtilDTO.ErrorResponse(1, "Unregistered email or incorrect password")
                );
            } else {
                return ResponseEntity.ok(new UserDTO.LoginResponse(
                        jwtTools.generateToken(user),
                        user.getId(),
                        user.getUsername(),
                        user.getRole()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new UtilDTO.ErrorResponse(500, "Internal server error"));
        }
    }
}
