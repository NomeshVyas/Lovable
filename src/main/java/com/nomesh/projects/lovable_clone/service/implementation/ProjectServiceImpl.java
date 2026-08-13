package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.project.ProjectRequest;
import com.nomesh.projects.lovable_clone.dto.project.ProjectResponse;
import com.nomesh.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.nomesh.projects.lovable_clone.entity.Project;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.mapper.ProjectMapper;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
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

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        return projectMapper.toListOfProjectSummaryResponse(
                projectRepository.findAllAccessibleByUser(userId)
        );
    }

    @Override
    public ProjectResponse getUserProject(Long id, Long userId) {
        return projectMapper.toProjectResponse(
            getAccessibleProjectById(id, userId)
        );
    }

    @Override
    public ProjectResponse createProject(Long userId, ProjectRequest request) {
        User owner = userRepository.findById(userId).orElseThrow();

        Project project = Project.builder()
                .name(request.name())
                .owner(owner)
                .build();

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = getAccessibleProjectById(id, userId);

        if (!project.getOwner().getId().equals(userId))
            throw new RuntimeException("Not allowerd to Update Project");

        project.setName(request.name());
        project.setUpdatedAt(Instant.now());
        return projectMapper.toProjectResponse(
                projectRepository.save(project)
        );
    }

    @Override
    public void softDeleteProject(Long id, Long userId) {
        Project project = getAccessibleProjectById(id, userId);

        if (!project.getOwner().getId().equals(userId))
            throw new RuntimeException("Not allowed to delete Project");

        project.setDeletedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);
    }

    // INTERNAL FUNCTIONS
    private Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectbyId(projectId, userId).orElseThrow();
    }
}
