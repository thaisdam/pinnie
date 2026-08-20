package com.pinnie.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

@Component
public class ImageProcessor {

    private final Tika tika = new Tika();
    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_WIDTH = 5000;
    private static final int MAX_HEIGHT = 5000;
    private static final long MAX_PIXELS = 25_000_000L;

    public ImageInfo processAndValidate(byte[] imageBytes) throws Exception {
        // Detect MIME Type via Magic Bytes
        String mimeType = tika.detect(imageBytes);
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP are allowed. Detected: " + mimeType);
        }

        // Read dimensions via ImageIO
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IllegalArgumentException("Corrupted or unreadable image file.");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            throw new IllegalArgumentException("Image dimensions exceed the maximum allowed (" + MAX_WIDTH + "x" + MAX_HEIGHT + ").");
        }

        long totalPixels = (long) width * height;
        if (totalPixels > MAX_PIXELS) {
            throw new IllegalArgumentException("Image exceeds the maximum total pixels allowed.");
        }

        return new ImageInfo(mimeType, width, height);
    }

    public static class ImageInfo {
        private final String mimeType;
        private final int width;
        private final int height;

        public ImageInfo(String mimeType, int width, int height) {
            this.mimeType = mimeType;
            this.width = width;
            this.height = height;
        }

        public String getMimeType() {
            return mimeType;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
