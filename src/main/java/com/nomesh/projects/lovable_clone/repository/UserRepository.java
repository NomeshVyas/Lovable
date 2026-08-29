package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String username, String email);


    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

    @Query("""
        SELECT u.email FROM User u
        WHERE u.id = :userId
    """)
    Optional<String> findEmailById(@Param("userId") Long id);

    default User getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
