package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.file.FileNode;
import com.nomesh.projects.lovable_clone.entity.ProjectFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}
