package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dao.UserAuth;
import com.onion.onionserver.repo.UserAuthRepo;
import com.onion.onionserver.repo.UserRepo;
import com.onion.onionserver.util.HashTools;
import com.onion.onionserver.util.RandTools;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationManager {
    private final UserRepo userRepo;
    private final UserAuthRepo userAuthRepo;

    public AuthenticationManager(UserRepo userRepo, UserAuthRepo userAuthRepo) {
        this.userRepo = userRepo;
        this.userAuthRepo = userAuthRepo;
    }

    public void createAuth(Long user_id, String password) {
        var user = userRepo.findById(user_id).orElse(null);
        if (user != null) {
            throw new RuntimeException("User not exist. This should not happen.");
        }

        String salt = RandTools.randString(32);
        String hashString = HashTools.stringToSHA256(password);
        hashString += salt;
        hashString = HashTools.stringToSHA256(hashString);

        UserAuth userAuth = new UserAuth();
        userAuth.setId(user_id);
        userAuth.setSalt(salt);
        userAuth.setHash(hashString);
        userAuthRepo.save(userAuth);
    }

    public User authenticate(String email, String password) {
        var user = userRepo.findByEmail(email);
        if (user == null) {
            return null;
        }

        UserAuth userAuth = user.getUserAuth();
        String hashString = HashTools.stringToSHA256(password);
        hashString += userAuth.getSalt();
        hashString = HashTools.stringToSHA256(hashString);

        if (hashString.equals(userAuth.getHash())) {
            return user;
        } else {
            return null;
        }
    }

    public boolean verifyPassword(Long user_id, String password) {
        UserAuth userAuth = userAuthRepo.findById(user_id).orElse(null);

        if (userAuth == null) {
            throw new EntityNotFoundException();
        }

        String hashString = HashTools.stringToSHA256(password);
        hashString += userAuth.getSalt();
        hashString = HashTools.stringToSHA256(hashString);

        return hashString.equals(userAuth.getHash());
    }

    public void updatePassword(Long user_id, String newPassword) {
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
