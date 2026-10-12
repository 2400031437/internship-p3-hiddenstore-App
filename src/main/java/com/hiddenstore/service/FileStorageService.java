package com.hiddenstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /** Saves an uploaded image and returns the stored file name (or null if nothing valid was uploaded). */
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) return null;
        String original = file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) return null;
        String name = UUID.randomUUID() + "." + ext;
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name));
            return name;
        } catch (IOException e) {
            throw new IllegalStateException("Could not save image", e);
        }
    }
}
