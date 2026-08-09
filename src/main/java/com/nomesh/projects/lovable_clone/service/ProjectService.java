package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.project.ProjectRequest;
import com.nomesh.projects.lovable_clone.dto.project.ProjectResponse;
import com.nomesh.projects.lovable_clone.dto.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects(Long userId);

    ProjectResponse getUserProject(Long id, Long userId);

    ProjectResponse createProject(Long userId, ProjectRequest request);

    ProjectResponse updateProject(Long id, ProjectRequest request, Long userId);

    void softDeleteProject(Long id, Long userId);
}
