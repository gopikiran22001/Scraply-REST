package com.scraply.rest.dto.request;

import com.scraply.rest.enums.RequestCategory;
import com.scraply.rest.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewRequestBody {

    @NotNull
    private RequestType requestType;

    @NotNull
    private RequestCategory requestCategory;

    @NotBlank
    private String description;

    @NotNull
    private MultipartFile image;

    @NotBlank
    private String address;

    @NotNull
    private Integer pinCode;

    private String landMark;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

}
