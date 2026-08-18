package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.project.ProjectRequest;
import com.nomesh.projects.lovable_clone.dto.project.ProjectResponse;
import com.nomesh.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.nomesh.projects.lovable_clone.entity.Project;
import com.nomesh.projects.lovable_clone.entity.ProjectMemberId;
import com.nomesh.projects.lovable_clone.entity.ProjectRole;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import com.nomesh.projects.lovable_clone.mapper.ProjectMapper;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

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

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        return projectMapper.toListOfProjectSummaryResponse(
                projectRepository.findAllAccessibleByUser(userId)
        );
    }

    @Override
    public ProjectResponse getUserProject(Long id) {
        Long userId = authUtil.getCurrentUserId();
        return projectMapper.toProjectResponse(
            getAccessibleProjectById(id)
        );
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
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
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id);

        project.setName(request.name());
        project.setUpdatedAt(Instant.now());
        return projectMapper.toProjectResponse(
                projectRepository.save(project)
        );
    }

    @Override
    public void softDeleteProject(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id);

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
