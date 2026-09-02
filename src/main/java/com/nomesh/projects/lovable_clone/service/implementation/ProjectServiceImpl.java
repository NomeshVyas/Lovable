package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.project.ProjectRequest;
import com.nomesh.projects.lovable_clone.dto.project.ProjectResponse;
import com.nomesh.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.nomesh.projects.lovable_clone.entity.*;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import com.nomesh.projects.lovable_clone.mapper.ProjectMapper;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.ProjectService;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    ProjectMemberServiceImpl projectMemberService;
    AuthUtil authUtil;
    SubscriptionService subscriptionService;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        return projectMapper.toListOfProjectSummaryResponse(
                projectRepository.findAllAccessibleByUser(userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectResponse getUserProject(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        return projectMapper.toProjectResponse(
            getAccessibleProjectById(projectId)
        );
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        if (!subscriptionService.canCreateNewProject()) {
            throw new BadRequestException("User cannot create a new project with current plan, Upgrade plan now.");
        }

        Long userId = authUtil.getCurrentUserId();
        User owner = userRepository.getReferenceById(userId);

        Project project = Project.builder()
                .name(request.name())
                .build();

        project = projectRepository.save(project);

        projectMemberService.addProjectMember(project, owner, ProjectRole.OWNER);

        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId);

        project.setName(request.name());
        project.setUpdatedAt(Instant.now());
        return projectMapper.toProjectResponse(
                projectRepository.save(project)
        );
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#projectId)")
    public void softDeleteProject(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId);

        project.setDeletedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
    }

    // INTERNAL FUNCTIONS
    private Project getAccessibleProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        return projectRepository.findAccessibleProjectbyId(projectId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Project", projectId)
                );
    }
}
