package com.scraply.rest.dto;

import com.scraply.rest.models.enums.ScrapCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PickupRequestBody {

    @NotBlank
    private String description;

    @NotNull
    private ScrapCategory category;

    @NotNull
    private MultipartFile image;

    // Location coordinates
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String address;

    @NotNull
    private int pinCode;
}