package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
