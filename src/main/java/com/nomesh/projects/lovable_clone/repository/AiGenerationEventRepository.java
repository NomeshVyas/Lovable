package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.AiGenerationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiGenerationEventRepository extends JpaRepository<AiGenerationEvent, Long> {

    List<AiGenerationEvent> findByRunIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(Long runId, Long sequenceNumber);
}
