package com.nomesh.projects.lovable_clone.service.file;

import com.nomesh.projects.lovable_clone.dto.file.FileContentResponse;
import com.nomesh.projects.lovable_clone.dto.file.FileNode;

import java.util.List;

public interface ProjectFileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFile(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
