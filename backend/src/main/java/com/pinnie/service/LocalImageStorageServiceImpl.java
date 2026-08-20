package com.pinnie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalImageStorageServiceImpl implements ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageStorageServiceImpl.class);
    private final Path rootLocation;

    public LocalImageStorageServiceImpl(@Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(byte[] content, String extension) throws IOException {
        String filename = UUID.randomUUID().toString() + extension;
        Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new SecurityException("Cannot store file outside current directory.");
        }

        Files.write(destinationFile, content);
        return filename;
    }

    @Override
    public void delete(String storedFilename) {
        try {
            Path file = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();
            if (!file.getParent().equals(this.rootLocation.toAbsolutePath())) {
                logger.warn("Security warning: Attempt to delete file outside upload directory: {}", storedFilename);
                return;
            }
            if (Files.exists(file)) {
                Files.delete(file);
            } else {
                logger.warn("Failed to delete orphaned file (Not found): {}", storedFilename);
            }
        } catch (IOException e) {
            logger.error("Failed to delete orphaned file: {}", storedFilename, e);
        }
    }
}
