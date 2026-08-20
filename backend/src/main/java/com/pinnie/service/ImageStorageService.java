package com.pinnie.service;

import java.io.IOException;

public interface ImageStorageService {
    String store(byte[] content, String extension) throws IOException;
    void delete(String storedFilename);
}
