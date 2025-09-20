package com.exotech.kindmap.repository;

import com.exotech.kindmap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {
}
