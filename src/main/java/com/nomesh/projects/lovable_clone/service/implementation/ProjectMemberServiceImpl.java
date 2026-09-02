package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.nomesh.projects.lovable_clone.dto.member.MemberResponse;
import com.nomesh.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.nomesh.projects.lovable_clone.entity.*;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import com.nomesh.projects.lovable_clone.mapper.ProjectMemberMapper;
import com.nomesh.projects.lovable_clone.repository.ProjectMemberRepository;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService  {

    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberMapper projectMemberMapper;
    AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@security.canViewProjectMembers(#projectId)")
    public List<MemberResponse> getProjectMembers(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        return projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toMemberResponse)
                .toList();
    }

    @Override
    @PreAuthorize("@security.canManageProjectMembers(#projectId)")
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId);
        User invitee = request.email() != null ?
            userRepository.findByEmail(request.email())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User", request.email())
                )
            :
            userRepository.findByUsername(request.username())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User", request.username())
                );

        if (invitee.getId().equals(userId))
            throw new RuntimeException("Cannot invite yourself");

        return projectMemberMapper.toMemberResponse(addProjectMember(project, invitee, request.role()));
    }

    @Override
    @PreAuthorize("@security.canManageProjectMembers(#projectId)")
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request) {
        Long userId = authUtil.getCurrentUserId();
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow(
                () -> new ResourceNotFoundException(projectMemberId)
        );
        projectMember.setProjectRole(request.role());

        return projectMemberMapper.toMemberResponse(
            projectMemberRepository.save(projectMember)
        );
    }

    @Override
    @PreAuthorize("@security.canManageProjectMembers(#projectId)")
    public void removeProjectMember(Long projectId, Long memberId) {
        Long userId = authUtil.getCurrentUserId();
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);

        if (!projectMemberRepository.existsById(projectMemberId))
            throw new ResourceNotFoundException(projectMemberId);

        projectMemberRepository.deleteById(projectMemberId);
    }

    @Override
    public ProjectMember addProjectMember(Project project, User user, ProjectRole projectRole) {
        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), user.getId());

        if (projectMemberRepository.existsById(projectMemberId))
            throw new RuntimeException("Cannot invite once again...");

        ProjectMember member = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(user)
                .projectRole(projectRole)
                .invitedAt(Instant.now())
                .acceptedAt(projectRole == ProjectRole.OWNER ? Instant.now() : null)
                .build();

        return projectMemberRepository.save(member);
    }

    // INTERNAL FUNCTIONS
    private Project getAccessibleProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        return projectRepository.findAccessibleProjectbyId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId)
            );
    }
}
