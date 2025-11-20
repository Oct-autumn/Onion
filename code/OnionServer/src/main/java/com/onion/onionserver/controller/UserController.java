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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> register(@AuthenticationPrincipal User authUser, @RequestBody UserDTO.RegisterRequest request) {
        if (authUser.getRole() == 1) {
            // admin注册用户时，无需指定密码，默认密码为"password123"
            if (request.getPassword() == null) {
                request.setPassword("password123");
            }
        }

        if (request.getEmail() == null || request.getPassword() == null || request.getUsername() == null) {
            return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(0));
        }

        // Verify email format
        if (!request.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(2, "Invalid email format"));
        }

        // Verify username format
        if (request.getUsername().length() < 2 || request.getUsername().length() > 64) {
            return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(3, "Username must be between 2 and 64 characters"));
        }

        try {
            User user = userManager.createUser(request.getEmail(), request.getUsername());
            authenticationManager.createAuth(user.getId(), request.getPassword());
            return ResponseEntity.ok().body(new UserDTO.RegisterResponse(user.getId(), user.getRole()));
        } catch (Exception e) {
            if (e.getMessage().equals("1")) {
                return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(1, "Email already registered"));
            } else {
                logger.warn("Unexpected error during registration", e);
                return ResponseEntity.internalServerError().body(new UtilDTO.ErrorResponse(500, "Internal server error"));
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
            return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(0));
        }

        try {
            User user = authenticationManager.authenticate(request.getEmail(), request.getPassword());
            if (user == null) {
                return ResponseEntity.status(401).body(new UtilDTO.ErrorResponse(1, "Unregistered email or incorrect password"));
            } else {
                return ResponseEntity.ok(new UserDTO.LoginResponse(jwtTools.generateToken(user), user.getId(), user.getUsername(), user.getRole()));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new UtilDTO.ErrorResponse(500, "Internal server error"));
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateInfo(@AuthenticationPrincipal User authUser, @RequestBody UserDTO.UpdateInfoRequest request) {
        if (request.getUser_id() != null && !request.getUser_id().equals(authUser.getId())) {
            // 非管理员不能修改其他用户信息
            if (authUser.getRole() != 1) {
                return ResponseEntity.status(403).body(new UtilDTO.ErrorResponse(0, "Forbidden"));
            }
        }

        var user = userManager.getUserById(request.getUser_id() == null ? authUser.getId() : request.getUser_id());

        if (user == null) {
            return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(1, "User not found"));
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != 0) {
            // 非管理员不能修改角色
            if (authUser.getRole() != 1) {
                return ResponseEntity.status(403).body(new UtilDTO.ErrorResponse(0, "Forbidden"));
            }
            user.setRole(request.getRole());
        }

        userManager.updateUser(user);

        return ResponseEntity.ok().build();
    }

    /**
     * 修改用户密码
     * <p>
     * admin可以修改任何用户密码，且不需要验证旧密码；
     * 普通用户只能修改自己的密码，且需要验证旧密码。
     */
    @PostMapping("/change_pwd")
    @ResponseBody
    public ResponseEntity<?> changePwd(@AuthenticationPrincipal User authUser, @RequestBody UserDTO.ChangePasswordRequest request) {
        if (authUser.getRole() == 1) {
            // admin修改任意用户密码
            if (request.getUser_id() == null) {
                return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(0, "User ID is required"));
            }
        } else {
            // 普通用户只能修改自己的密码
            if (request.getUser_id() != null && !request.getUser_id().equals(authUser.getId())) {
                return ResponseEntity.status(403).body(new UtilDTO.ErrorResponse(0, "Forbidden"));
            }
            request.setUser_id(authUser.getId());

            // 验证旧密码
            boolean verified = authenticationManager.verifyPassword(authUser.getId(), request.getOldPassword());
            if (!verified) {
                return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(1, "Old password is incorrect"));
            }
        }

        authenticationManager.updatePassword(request.getUser_id(), request.getNewPassword());

        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户信息列表
     * （对于admin，将按照分页返回所有用户信息；对于普通用户，只返回自己的信息）
     *
     * @param page
     * @param pagenum
     * @return
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal User authUser, @RequestParam("page") Integer page, @RequestParam("pagenum") Integer pagenum) {
        // 鉴权
        if (authUser.getRole() != 1) {
            // 非管理员只能获取自己的信息
            var resp = new UserDTO.GetInfoResponse();
            resp.setUsers(List.of(UserDTO.GetInfoResponse.UserInfo.fromDao(authUser)));
            return ResponseEntity.ok().body(resp);
        } else {
            // 管理员获取所有用户信息，分页返回
            if (page == null || pagenum == null || page <= 0 || pagenum <= 0) {
                return ResponseEntity.badRequest().body(new UtilDTO.ErrorResponse(0, "Invalid page or pagenum"));
            }

            var allUsers = userManager.getAllUsers(page, pagenum);
            var resp = new UserDTO.GetInfoResponse();
            resp.setUsers(allUsers.stream().map(UserDTO.GetInfoResponse.UserInfo::fromDao).toList());
            return ResponseEntity.ok().body(resp);
        }
    }
}
