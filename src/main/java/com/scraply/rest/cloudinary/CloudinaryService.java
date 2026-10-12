package com.scraply.rest.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.scraply.rest.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder-path}")
    private String folderPath;

    @Value("${scraply.image.max-file-size}")
    private long maxFileSize;

    @Value("${scraply.image.max-width}")
    private int maxWidth;

    @Value("${scraply.image.max-height}")
    private int maxHeight;

    @Value("${scraply.image.max-pixels}")
    private long maxPixels;

    /**
     * Uploads a MultipartFile to Cloudinary and returns the secure URL.
     *
     * @param file the image file to upload
     * @return the secure URL of the uploaded image
     * @throws BadRequestException if upload fails
     */
    public String uploadImage(MultipartFile file) {
        try {
            validateImage(file);
        } catch (Exception e) {
            throw new BadRequestException("Error uploading image");
        }
        return uploadImage(file, folderPath);
    }

    /**
     * Uploads a MultipartFile to Cloudinary with a custom folder.
     *
     * @param file   the image file to upload
     * @param folder the folder path in Cloudinary
     * @return the secure URL of the uploaded image
     * @throws BadRequestException if upload fails
     */
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Deletes an image from Cloudinary by its public ID.
     *
     * @param publicId the public ID of the image to delete
     */
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new BadRequestException("Failed to delete image: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile image) throws IOException {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }

        if (image.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    "Image size must not exceed " +
                            (maxFileSize / (1024 * 1024)) + " MB");
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                !Set.of("image/jpeg", "image/png")
                        .contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Only JPEG and PNG images are allowed");
        }

        try (ImageInputStream stream = ImageIO.createImageInputStream(
                new ByteArrayInputStream(image.getBytes()))) {

            if (stream == null) {
                throw new IllegalArgumentException("Invalid image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Invalid or unsupported image content");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(stream, true, true);

                String format = reader.getFormatName()
                        .toUpperCase(Locale.ROOT);

                if (!Set.of("JPEG", "PNG").contains(format)) {
                    throw new IllegalArgumentException(
                            "Only JPEG and PNG images are allowed");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0
                        || width > maxWidth
                        || height > maxHeight
                        || (long) width * height > maxPixels) {
                    throw new IllegalArgumentException(
                            "Image dimensions exceed the allowed limit");
                }

                if (reader.read(0) == null) {
                    throw new IllegalArgumentException(
                            "Unable to decode image");
                }

            } finally {
                reader.dispose();
            }
        }
    }
}