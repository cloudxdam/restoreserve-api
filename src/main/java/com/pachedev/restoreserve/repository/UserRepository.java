package com.pachedev.restoreserve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pachedev.restoreserve.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}