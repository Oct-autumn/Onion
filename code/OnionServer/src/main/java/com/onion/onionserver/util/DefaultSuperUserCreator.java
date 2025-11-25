package com.onion.onionserver.util;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.model.dao.UserAuth;
import com.onion.onionserver.repo.UserAuthRepo;
import com.onion.onionserver.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DefaultSuperUserCreator implements CommandLineRunner {
    public final static Logger log = LoggerFactory.getLogger(DefaultSuperUserCreator.class);
    private final UserRepo userRepo;
    private final UserAuthRepo userAuthRepo;

    @Autowired
    public DefaultSuperUserCreator(UserRepo userRepo, UserAuthRepo userAuthRepo) {
        this.userRepo = userRepo;
        this.userAuthRepo = userAuthRepo;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking admin account...");

        var result = userRepo.findById(1);
        if (result.isEmpty()) {
            var newUser = new User();
            newUser.setUsername("admin");
            newUser.setEmail("admin@onion.com");
            newUser.setRole(1);
            LocalDateTime now = LocalDateTime.now();
            newUser.setCreateAt(now);
            newUser.setUpdateAt(now);
            newUser = userRepo.save(newUser);

            String salt = RandTools.randString(32);
            String hashString = HashTools.stringToSHA256("AdminAdmin");
            hashString += salt;
            hashString = HashTools.stringToSHA256(hashString);
            UserAuth userAuth = new UserAuth();
            userAuth.setUser(newUser);
            userAuth.setSalt(salt);
            userAuth.setHash(hashString);
            userAuthRepo.save(userAuth);
            log.info("Admin account created with default password 'AdminAdmin'. Please change the password after first login.");
        }
    }
}
