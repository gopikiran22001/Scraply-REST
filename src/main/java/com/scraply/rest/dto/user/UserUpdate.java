package com.scraply.rest.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdate {
    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String address;

    private Integer pinCode;

    private MultipartFile profileImage;
}
