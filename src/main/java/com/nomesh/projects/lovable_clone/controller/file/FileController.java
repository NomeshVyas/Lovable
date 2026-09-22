package com.nomesh.projects.lovable_clone.controller.file;

import com.nomesh.projects.lovable_clone.dto.file.FileContentResponse;
import com.nomesh.projects.lovable_clone.dto.file.FileNode;
import com.nomesh.projects.lovable_clone.service.file.ProjectFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FileController {

    ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/{*path}")
    public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId, @PathVariable String path) {
        return ResponseEntity.ok(projectFileService.getFile(projectId, path));
    }
}
