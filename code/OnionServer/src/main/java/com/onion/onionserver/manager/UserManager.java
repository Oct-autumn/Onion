package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.repo.UserRepo;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public void updateUser(User user) {
        if (userRepo.existsById(user.getId()))    {
            user.setUpdateAt(LocalDateTime.now());
            userRepo.save(user);
        }
    }

    public List<User> getAllUsers(Integer page, Integer pagenum) {
        int offset = (page - 1) * pagenum;
        var pages = userRepo.findAll(Pageable.ofSize(pagenum).withPage(offset / pagenum));
        return pages.getContent();
    }
}
