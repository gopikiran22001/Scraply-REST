package com.scraply.rest.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.scraply.rest.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a MultipartFile to Cloudinary and returns the secure URL.
     *
     * @param file the image file to upload
     * @return the secure URL of the uploaded image
     * @throws BadRequestException if upload fails
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "scraply/profile-images",
                            "resource_type", "image"
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
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

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
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
            log.info("Image deleted successfully from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary", e);
            throw new BadRequestException("Failed to delete image: " + e.getMessage());
        }
    }
}
