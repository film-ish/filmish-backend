package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.UserRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.controller.response.UserResponse;
import com.example.knockknock.entity.Role;
import com.example.knockknock.entity.User;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.global.config.jwt.TokenProvider;
import com.example.knockknock.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenListService tokenListService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${image.resize.profile.width}")
    private int profileWidth;

    @Value("${image.resize.profile.height}")
    private int profileHeight;

    // 회원가입
    public ApiResponse join(UserRequest.JoinRequest joinRequest) {
        String userEmail = joinRequest.getEmail();
        String password = joinRequest.getPassword();
        String nickname = joinRequest.getNickname();
        Date birth = joinRequest.getBirth();

        log.info("joinProcess(), userEmail = " + userEmail);

        User data = User.builder()
                .email(userEmail)
                .password(bCryptPasswordEncoder.encode(password))
                .nickname(nickname)
                .birth(birth)
                .role(Role.USER)
                .active(true)
                .build();

        userRepository.save(data);

        return ApiSuccessResponse.response(ResponseCode.Created, "Join request success!", null);
    }

    @Transactional
    public ApiResponse withdraw(Long userId, HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ObjectMapper mapper = new ObjectMapper();

        try {
            User userEntity = userRepository.findById(userId).get();
            String userEmail = userEntity.getEmail();

            // get refresh token
            String refresh = null;
            Cookie[] cookies = request.getCookies();
            log.info("in UserServiceImpl, cookies = " + cookies);
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }

            // refresh null check
            if (refresh == null) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Refresh token does not exist");
            }

            // expired check
            try {
                tokenProvider.isExpired(refresh);
            } catch (ExpiredJwtException e) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "The token is expired");
            }

            // 토큰이 refresh인지 확인
            String category = tokenProvider.getCategory(refresh);
            if (!category.equals("refresh")) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token category");
            }

            // BlackList에 Access token이 저장되어 있는지 확인하고
            String access = request.getHeader("access");
            boolean isBlacked = tokenBlacklistService.isContainToken("BL:AT:" + access);
            if (isBlacked) {
                return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token");
            }

            tokenBlacklistService.addTokenToList("BL:AT:" + access);

            // 회원 상태(status) false 전환
            userEntity.setActive(false);
            log.info("in UserServiceImpl, userId = " + userId);

            // Refresh Token Cookie 값 0
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            ApiResponse apiResponse = ApiSuccessResponse.response(ResponseCode.Ok, "Withdraw request success!", null);
            String json = mapper.writeValueAsString(apiResponse);
            response.getWriter().write(json);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ApiErrorResponse.of(ErrorCode.AUTH_ERROR, "Invalid token");
        }
        // @Transactional 어노테이션을 사용하여 JPA가 트랜잭션 내에서 자동으로 변경 사항을 flush하여 DB에 반영함
    }

    public ApiResponse checkEmail(String email) {
        Optional<User> userEntity = userRepository.findByEmail(email);

        // 동일한 이메일이 존재한다면,
        if (!userEntity.isEmpty()) {
            Map<String, String> data = new HashMap<>();
            data.put("email", email);
            return ApiSuccessResponse.response(ResponseCode.Ok, "사용할 수 없는 이메일입니다.", data);
        }
        // 동일한 이메일이 존재하지 않는다면,
        return ApiSuccessResponse.response(ResponseCode.Ok, "사용할 수 있는 이메일입니다.", null);
    }

    public ApiResponse checkNickname(String nickname) {
        Optional<User> userEntity = userRepository.findByNickname(nickname);

        // 동일한 닉네임이 존재한다면,
        if (!userEntity.isEmpty()) {
            Map<String, String> data = new HashMap<>();
            data.put("nickname", nickname);
            return ApiSuccessResponse.response(ResponseCode.Ok, "사용할 수 없는 닉네임입니다.", data);
        }
        // 동일한 닉네임이 존재하지 않는다면,
        return ApiSuccessResponse.response(ResponseCode.Ok, "사용할 수 있는 닉네임입니다.", null);
    }

    //회원 정보 조회
    public ApiResponse userInfo(Long userId) {
        User userEntity = userRepository.findById(userId).get();
        UserResponse.UserInfoResponse infoResponse = UserResponse.UserInfoResponse.of(userEntity);
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회하였습니다.", infoResponse);
    }

    // 회원 정보 수정
    public ApiResponse updateUser(Long userId, String nickname, String image) {
        String imagePath = null;
        String compressedPath = null;

        byte[] compressed = compressImage(imagePath);

        if (compressed == null) {
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미지 읽어오기 실패");
        }

        //TODO
        /*
            byte로 압축된 이미지(compressed) 저장 경로 생성 필요
            compressedPath = {압축 이미지 저장 경로}
         */

        User userEntity = userRepository.findById(userId).get();
        userEntity.setNickname(nickname);
        userEntity.setImage(imagePath);
        userEntity.setHead_image(compressedPath);
        userRepository.save(userEntity);
        return ApiSuccessResponse.response(ResponseCode.Ok, "정보 수정이 완료되었습니다.", null);
    }

    // 비밀 번호 수정
    public ApiResponse modifyPassword(Authentication authentication, String newPassword) {
        try {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            User userEntity = userRepository.findById(customUserDetails.getUserId()).get();
            userEntity.setPassword(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(userEntity);
            return ApiSuccessResponse.response(ResponseCode.Ok, "비밀번호를 성공적으로 수정하였습니다.", null);
        } catch (Exception e) {
            log.error("Failed to update password: " + e.getMessage());
            return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "Failed to update password");
        }
    }

    // 이미지 압축
    public byte[] compressImage(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            log.info("이미지 경로가 유효하지 않습니다.");
            return null;
        }

        // 파일 읽어오기
        File inputFile = new File(imagePath);

        // 파일 존재 여부 확인
        if (!inputFile.exists()) {
            log.info("지정된 경로에 파일이 존재하지 않습니다: " + imagePath);
        }

        BufferedImage originalImage = null;

        // 원본 이미지 읽어오기
        try {
            originalImage = ImageIO.read(inputFile);
        } catch (IOException e) {
            log.info("원본 이미지를 읽을 수 없습니다 : " + e);
            return null;
        }

        // 이미지 읽기 실패 확인
        if (originalImage == null) {
            log.info("이미지를 읽을 수 없습니다: " + imagePath);
        }

        // 새로운 크기의 빈 BufferedImage 생성
        //TODO
        // type을 어떻게 할 것인지?
        BufferedImage resizedImage = new BufferedImage(profileWidth, profileHeight, originalImage.getType());

        // Graphics2D를 사용하여 원본 이미지를 새 크기로 그림
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, profileWidth, profileHeight, null);
        g2d.dispose();                          // Graphics2D 리소스 해제

        // 결과 이미지를 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.info("압축 이미지 저장 중 오류 발생 : " + e);
            return null;
        }
    }
}
