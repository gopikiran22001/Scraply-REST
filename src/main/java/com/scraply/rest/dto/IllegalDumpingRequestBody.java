package com.scraply.rest.dto;

import com.scraply.rest.models.enums.ScrapCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class IllegalDumpingRequestBody {

    @NotBlank
    private String description;

    private ScrapCategory category;

    @NotNull
    private MultipartFile image;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String address;

    private String landmark;
}
