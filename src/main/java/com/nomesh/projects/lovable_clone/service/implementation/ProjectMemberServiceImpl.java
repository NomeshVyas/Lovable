package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.nomesh.projects.lovable_clone.dto.member.MemberResponse;
import com.nomesh.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.nomesh.projects.lovable_clone.entity.Project;
import com.nomesh.projects.lovable_clone.entity.ProjectMember;
import com.nomesh.projects.lovable_clone.entity.ProjectMemberId;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.mapper.ProjectMemberMapper;
import com.nomesh.projects.lovable_clone.repository.ProjectMemberRepository;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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

    @Override
    public List<MemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        List<MemberResponse> members = new ArrayList<>();
        members.add(
                projectMemberMapper.toMemberResponse(project.getOwner())
        );
        members.addAll(
            projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper :: toMemberResponse)
                .toList()
        );
        return members;
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        if(!project.getOwner().getId().equals(userId))
            throw new RuntimeException("Not Allowed - only project owner can invite");

        User invitee = userRepository.findByEmail(request.email()).orElseThrow();

        if (invitee.getId().equals(userId))
            throw new RuntimeException("Cannot invite yourself");

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());

        if (projectMemberRepository.existsById(projectMemberId))
            throw new RuntimeException("Cannot invite once again...");

        ProjectMember member = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(invitee)
                .projectRole(request.role())
                .invitedAt(Instant.now())
                .build();

        projectMemberRepository.save(member);
        return projectMemberMapper.toMemberResponse(member);
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        if (!project.getOwner().getId().equals(userId))
            throw new RuntimeException("Not Allowed - only owner can update project member's role");

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();
        projectMember.setProjectRole(request.role());

        return projectMemberMapper.toMemberResponse(
            projectMemberRepository.save(projectMember)
        );
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        if (!project.getOwner().getId().equals(userId))
            throw new RuntimeException("Not Allowed - only owner can remove project member");

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);

        if (!projectMemberRepository.existsById(projectMemberId))
            throw new RuntimeException("Not Found - member not found in project");

        projectMemberRepository.deleteById(projectMemberId);
    }

    // INTERNAL FUNCTIONS
    private Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectbyId(projectId, userId).orElseThrow();
    }
}
