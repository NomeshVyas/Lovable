package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.ProjectMember;
import com.nomesh.projects.lovable_clone.entity.ProjectMemberId;
import com.nomesh.projects.lovable_clone.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId);

    @Query(
        """
        SELECT pm.projectRole FROM ProjectMember pm
        WHERE pm.id.projectId = :projectId
        AND pm.id.userId = :userId
        """
    )
    Optional<ProjectRole> findProjectRoleByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query(
        """
        SELECT COUNT(pm) FROM ProjectMember pm
        WHERE pm.id.userId = :userId AND pm.projectRole = 'OWNER'
        """
    )
    long countProjectsOwnedByUser(@Param("userId") Long userId);
}
