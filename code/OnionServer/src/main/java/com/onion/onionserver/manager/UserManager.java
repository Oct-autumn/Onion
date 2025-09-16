package com.onion.onionserver.manager;

import com.onion.onionserver.model.dao.User;
import com.onion.onionserver.repo.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserManager {

    UserRepo userRepo;

    public UserManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
}
