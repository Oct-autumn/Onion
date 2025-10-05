package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.repo.UserRepo;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class UserManager {

    private final UserRepo userRepo;

    public UserManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User createUser(String email, String username) {
        if (userRepo.findByEmail(email) != null) {
            throw new RuntimeException("1"); // Email already registered
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setRole(0);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateAt(now);
        user.setUpdateAt(now);
        return userRepo.save(user);
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }
}
