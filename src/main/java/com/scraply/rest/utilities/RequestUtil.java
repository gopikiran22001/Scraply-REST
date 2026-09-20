package com.scraply.rest.utilities;

import com.scraply.rest.cloud.CloudinaryService;
import com.scraply.rest.dto.request.RequestBodyDTO;
import com.scraply.rest.dto.request.RequestResponse;
import com.scraply.rest.mapper.RequestMapper;
import com.scraply.rest.model.Request;
import com.scraply.rest.repo.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestUtil {

    private final CloudinaryService cloudinaryService;

    private final RequestRepository requestRepository;

    private final RequestMapper requestMapper;


    public RequestResponse create(RequestBodyDTO requestBodyDTO) {
        String imageUrl = cloudinaryService.uploadImage(requestBodyDTO.getImage());

        Request request = requestMapper.toEntity(requestBodyDTO, imageUrl);

        requestRepository.save(request);

        return requestMapper.toResponse(request);

    }
}
