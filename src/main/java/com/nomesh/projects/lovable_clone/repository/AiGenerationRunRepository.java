package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.AiGenerationRun;
import com.nomesh.projects.lovable_clone.entity.AiGenerationRunStatus;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AiGenerationRunRepository extends JpaRepository<AiGenerationRun, Long> {

    Optional<AiGenerationRun> findFirstByProjectIdAndStatusIn(
            Long projectId,
            Collection<AiGenerationRunStatus> statuses
    );

    List<AiGenerationRun> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    default AiGenerationRun findByIdOrThrow(Long runId) {
        return findById(runId).orElseThrow(() ->
                new ResourceNotFoundException("AiGenerationRun", runId)
        );
    }

    List<AiGenerationRun> findByStatusIn(List<AiGenerationRunStatus> statuses);
}
