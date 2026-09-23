package com.scraply.rest.utilities;

import com.scraply.rest.common.PageResponse;
import com.scraply.rest.dto.auth.SignInReq;
import com.scraply.rest.dto.auth.SignUpReq;
import com.scraply.rest.dto.user.PickerDetailsUpdate;
import com.scraply.rest.dto.user.UserResponse;
import com.scraply.rest.dto.user.UserUpdate;
import com.scraply.rest.enums.AccountStatus;
import com.scraply.rest.exception.DuplicateResourceException;
import com.scraply.rest.exception.ResourceNotFoundException;
import com.scraply.rest.mapper.UserMapper;
import com.scraply.rest.model.User;
import com.scraply.rest.repo.UserRepository;
import com.scraply.rest.security.CookieUtil;
import com.scraply.rest.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserUtility {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final JwtService jwtService;

    private final CookieUtil cookieUtil;

    private final SecurityUtility securityUtility;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserResponse create(SignUpReq request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email Already Exist");

        User user = userMapper.toEntity(request);

        userRepository.save(user);

        cookieSetter(response, user);

        return userMapper.toResponse(user);
    }

    public UserResponse sigIn(SignInReq request, HttpServletResponse httpServletResponse) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        if(!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid Credentials");
        }

        if (!user.getStatus().equals(AccountStatus.ACCEPTED)) {
            throw new ResourceNotFoundException("Account Not Accepted Yet");
        }

        cookieSetter(httpServletResponse, user);

        return userMapper.toResponse(user);
    }

    public String logout(HttpServletResponse response) {
        cookieUtil.clearAuthenticationCookies(response);
        return "Logged Out Successfully";
    }

    private void cookieSetter(HttpServletResponse httpServletResponse, User user) {
        cookieUtil.setTokenCookie(httpServletResponse, jwtService.generateToken(user));
    }

    public UserResponse setAccountStatus(User user, AccountStatus accountStatus) {
        user.setStatus(accountStatus);
        return userMapper.toResponse(user);
    }

    public PageResponse<UserResponse> getPickers(AccountStatus accountStatus, Integer pinCode, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<UserResponse> pickerPage = userRepository.findPickers(accountStatus, pinCode, pageable);

        return PageResponse.from(pickerPage);
    }

    public UserResponse updateProfile(UserUpdate userUpdate) {
        User user = securityUtility.getCurrentUser();

        user = userMapper.toEntity(user, userUpdate);

        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    public UserResponse updatePickerDetails(User picker, PickerDetailsUpdate pickerDetailsUpdate) {
        picker = userMapper.toEntity(picker, pickerDetailsUpdate);

        userRepository.save(picker);

        return userMapper.toResponse(picker);
    }
}
