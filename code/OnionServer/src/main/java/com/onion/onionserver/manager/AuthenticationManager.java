package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dao.UserAuth;
import com.onion.onionserver.repo.UserAuthRepo;
import com.onion.onionserver.repo.UserRepo;
import com.onion.onionserver.util.HashTools;
import com.onion.onionserver.util.RandTools;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Service
public class AuthenticationManager {
    private final UserRepo userRepo;
    private final UserAuthRepo userAuthRepo;

    public AuthenticationManager(UserRepo userRepo, UserAuthRepo userAuthRepo) {
        this.userRepo = userRepo;
        this.userAuthRepo = userAuthRepo;
    }

    @PostConstruct
    public void init() {
        User user;
        {
            var result = userRepo.findById(1);
            if (result.isPresent()) {
                user = result.get();
            } else {
                var newUser = new User();
                newUser.setUsername("admin");
                newUser.setEmail("admin@onion.com");
                newUser.setRole(1);
                LocalDateTime now = LocalDateTime.now();
                newUser.setCreateAt(now);
                newUser.setUpdateAt(now);
                userRepo.save(newUser);
                user = newUser;
            }
        }

        {
            var result = userAuthRepo.findById(user.getId());
            if (result.isEmpty()) {
                createAuth(user.getId(), "AdminAdmin");
            }
        }
    }

    public void createAuth(Integer user_id, String password) {
        var user = userRepo.findById(user_id).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not exist. This should not happen.");
        }

        String salt = RandTools.randString(32);
        String hashString = HashTools.stringToSHA256(password);
        hashString += salt;
        hashString = HashTools.stringToSHA256(hashString);

        UserAuth userAuth = new UserAuth();
        userAuth.setUser(user);
        userAuth.setSalt(salt);
        userAuth.setHash(hashString);
        userAuthRepo.save(userAuth);
    }

    public User authenticate(String email, String password) {
        var user = userRepo.findByEmail(email);
        if (user == null) {
            return null;
        }

        UserAuth userAuth = requireNonNull(userAuthRepo.findById(user.getId()).orElse(null), "UserAuth not found for user. This should not happen.");
        String hashString = HashTools.stringToSHA256(password);
        hashString += userAuth.getSalt();
        hashString = HashTools.stringToSHA256(hashString);

        if (hashString.equals(userAuth.getHash())) {
            return user;
        } else {
            return null;
        }
    }

    public boolean verifyPassword(Integer user_id, String password) {
        UserAuth userAuth = userAuthRepo.findById(user_id).orElse(null);

        if (userAuth == null) {
            throw new EntityNotFoundException();
        }

        String hashString = HashTools.stringToSHA256(password);
        hashString += userAuth.getSalt();
        hashString = HashTools.stringToSHA256(hashString);

        return hashString.equals(userAuth.getHash());
    }

    public void updatePassword(Integer user_id, String newPassword) {
        UserAuth userAuth = userAuthRepo.findById(user_id).orElse(null);

        if (userAuth == null) {
            throw new EntityNotFoundException();
        }

        String newSalt = RandTools.randString(32);
        String hashString = HashTools.stringToSHA256(newPassword);
        hashString += newSalt;
        hashString = HashTools.stringToSHA256(hashString);

        userAuth.setSalt(newSalt);
        userAuth.setHash(hashString);
        userAuthRepo.save(userAuth);
    }
}
