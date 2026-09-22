package com.nomesh.projects.lovable_clone.service.file;

import com.nomesh.projects.lovable_clone.config.MinioProperties;
import com.nomesh.projects.lovable_clone.dto.file.FileContentResponse;
import com.nomesh.projects.lovable_clone.dto.file.FileNode;
import com.nomesh.projects.lovable_clone.entity.Project;
import com.nomesh.projects.lovable_clone.entity.ProjectFile;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.exception.StorageException;
import com.nomesh.projects.lovable_clone.mapper.ProjectFileMapper;
import com.nomesh.projects.lovable_clone.repository.ProjectFileRepository;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectFileServiceImpl implements ProjectFileService {

    ProjectRepository projectRepository;
    ProjectFileRepository projectFileRepository;
    MinioClient minioClient;
    MinioProperties minioProperties;
    ProjectFileMapper projectFileMapper;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@security.canViewProject(#projectId)")
    public List<FileNode> getFileTree(Long projectId) {
        return projectFileMapper.toListOfFileNode(
            projectFileRepository.findByProjectId(projectId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@security.canViewProject(#projectId)")
    public FileContentResponse getFile(Long projectId, String path) {
        return null;
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
//        Project project = projectRepository.getByIdOrThrow(projectId);
//
//        String cleanPath = normalizeFilePath(filePath);
//        String objectKey = projectId + "/" + cleanPath;
//
//        try {
//            byte[] contentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
//            InputStream inputStream = new ByteArrayInputStream(contentBytes);
//
//            // Saving the file content
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(minioProperties.projectBucket())
//                            .object(objectKey)
//                            .stream(inputStream, (long) contentBytes.length, -1L)
//                            .contentType(determineContentType(filePath))
//                            .build()
//            );
//
//            // Saving the metadata
//            ProjectFile projectFile = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
//                    .orElseGet(() -> ProjectFile.builder()
//                            .project(project)
//                            .path(cleanPath)
//                            .minioObjectKey(objectKey)
//                            .build()
//                    );
//
//            projectFile.setUpdatedAt(Instant.now());
//            projectFileRepository.save(projectFile);
//            log.info("Saved file on Minio: {}", objectKey);
//        } catch (Exception exception) {
//            log.error("Failed to save file {} / {}", projectId, cleanPath, exception);
//            throw new StorageException("File save failed on Minio", exception);
//        }
    }

    private String determineContentType(String filePath) {
        String contentType = URLConnection.guessContentTypeFromName(filePath);
        if (contentType != null) return contentType;
        if (filePath.endsWith(".jsx") || filePath.endsWith(".ts") || filePath.endsWith(".tsx")) return "text/javascript";
        if (filePath.endsWith(".json")) return "application/json";
        if (filePath.endsWith(".css")) return "text/css";

        return "text/plain";
    }

    private String normalizeFilePath(String filePath) {
        if (filePath.isBlank() || filePath.contains("..") || filePath.contains("\\"))
            throw new BadRequestException("Invalid file path: " + filePath);

        return filePath.startsWith("/") ? filePath.substring(1) : filePath;
    }

}
