package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.project.ProjectResponse;
import com.nomesh.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.nomesh.projects.lovable_clone.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    @Mapping(source = "name", target = "projectName")
    ProjectSummaryResponse toProjectSummaryResponse(Project project);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);
}
