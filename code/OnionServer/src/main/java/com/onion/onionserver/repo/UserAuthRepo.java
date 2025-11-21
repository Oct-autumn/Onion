package com.onion.onionserver.repo;

import com.onion.onionserver.model.dao.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserAuthRepo extends JpaRepository<UserAuth, Integer> {
}
