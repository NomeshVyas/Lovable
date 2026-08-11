package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.file.FileContentResponse;
import com.nomesh.projects.lovable_clone.dto.file.FileNode;
import com.nomesh.projects.lovable_clone.service.FileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public List<FileNode> getFileTree(Long projectId, Long userId) {
        return List.of();
    }

    @Override
    public FileContentResponse getFile(Long projectId, String path, Long userId) {
        return null;
    }
}
