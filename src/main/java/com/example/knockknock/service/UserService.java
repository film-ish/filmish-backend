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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final S3Service s3Service;

    @Value("${image.resize.profile.width}")
    private int profileWidth;

    @Value("${image.resize.profile.height}")
    private int profileHeight;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // 회원가입
    public ApiResponse join(UserRequest.Join join) {
        String userEmail = join.getEmail();
        String password = join.getPassword();
        String nickname = join.getNickname();
        LocalDate birth = join.getBirth();
        MultipartFile imageFile = (join.getImage() == null || join.getImage().isEmpty()) ? null : join.getImage();

        log.info("joinProcess(), userEmail = " + userEmail);

        User data = User.builder()
                .email(userEmail)
                .password(bCryptPasswordEncoder.encode(password))
                .nickname(nickname)
                .birth(birth)
                .role(Role.USER)
                .active(true)
                .build();

        User saved = userRepository.save(data);

        String imagePath = null;
        String compressPath = null;

        if (imageFile != null) {
            try {
                imagePath = saveImage(saved.getId(), imageFile);
                compressPath = saveCompressImage(saved.getId(), imageFile);
                saved.setImage(imagePath);
                saved.setHeadImage(compressPath);
                userRepository.save(saved);
            } catch (Exception e) {
                log.error("이미지 저장 중 오류 발생", e);
            }
        }


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
    public ApiResponse updateUser(Long userId, UserRequest.Modify Modify, CustomUserDetails userDetails) {
        // 본인이 아닌 경우 수정 불가능
        if (userDetails.getUserId() != userId){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "수정 권한이 없습니다.");
        }

        MultipartFile imageFile = Modify.getImage();
        String nickname = Modify.getNickname();
        User userEntity = null;

        // 사용자 조회
        try{
            userEntity = userRepository.findById(userId).get();
        } catch (RuntimeException e) {
            log.info("사용자를 찾을 수 없습니다.");
            return ApiErrorResponse.of(ErrorCode.NOT_FOUND, "사용자 정보가 존재하지 않습니다.");
        }

        // 이미지가 없는 경우 닉네임만 업데이트
        if (imageFile == null || imageFile.isEmpty()) {
            userEntity.setNickname(nickname);
            userRepository.save(userEntity);
            return ApiSuccessResponse.response(ResponseCode.Ok, "닉네임 수정이 완료되었습니다.", null);
        }

        // 기존 이미지가 있다면 S3에서 삭제
        if (userEntity.getImage() != null && !userEntity.getImage().isEmpty()) {
            String oldImageKey = extractKeyFromUrl(userEntity.getImage());
            if (oldImageKey != null) {
                s3Service.deleteFile(bucketName, oldImageKey);
            }
        }

        // 기존 압축 이미지가 있다면 S3에서 삭제
        if (userEntity.getHeadImage() != null && !userEntity.getHeadImage().isEmpty()) {
            String oldCompressedKey = extractKeyFromUrl(userEntity.getHeadImage());
            if (oldCompressedKey != null) {
                s3Service.deleteFile(bucketName, oldCompressedKey);
            }
        }

        String imagePath = saveImage(userId, imageFile);
        String compressedPath = saveCompressImage(userId, imageFile);

        if (imagePath == null || compressedPath == null){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미지 저장 중 오류가 발생했습니다.");
        }

        // 사용자 정보 업데이트
        userEntity.setNickname(nickname);
        userEntity.setImage(imagePath);
        userEntity.setHeadImage(compressedPath);
        userRepository.save(userEntity);

        return ApiSuccessResponse.response(ResponseCode.Ok, "정보 수정이 완료되었습니다.", null);
    }

    private String saveImage(Long userId, MultipartFile imageFile){
        String originalFileName = null;
        try {
            // 원본 이미지 S3에 업로드
            originalFileName = imageFile.getOriginalFilename();
        }catch (Exception e){
            log.warn("originalFileName이 null이거나 확장자가 없습니다.");
            return null;
        }
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String originalKey = "images/original/" + userId + "_" + System.currentTimeMillis() + fileExtension;
        String imagePath = null;
        try {
            // S3에 원본 이미지 업로드
            s3Service.uploadFile(bucketName, originalKey, imageFile.getBytes(), imageFile.getContentType());
            imagePath = "https://" + bucketName + ".s3.amazonaws.com/" + originalKey;
        } catch (IOException e){
            log.info("이미지 저장 중 오류 발생");
            return null;
        }
        return imagePath;
    }

    private String saveCompressImage(Long userId, MultipartFile imageFile) {
        // 이미지 압축
        byte[] compressedImageBytes = compressImage(imageFile);
        if (compressedImageBytes == null) {
            log.info("이미지 압축 실패");
            return null;
        }
        // 압축 이미지 S3에 업로드
        String compressedKey = "images/compressed/" + userId + "_" + System.currentTimeMillis() + ".jpg";
        s3Service.uploadFile(bucketName, compressedKey, compressedImageBytes, "image/jpeg");
        String compressedPath = "https://" + bucketName + ".s3.amazonaws.com/" + compressedKey;
        return compressedPath;
    }


    // 비밀 번호 수정
    public ApiResponse modifyPassword(CustomUserDetails userDetails, String newPassword) {
        try {
            User userEntity = userRepository.findById(userDetails.getUserId()).get();
            userEntity.setPassword(bCryptPasswordEncoder.encode(newPassword));
            userRepository.save(userEntity);
            return ApiSuccessResponse.response(ResponseCode.Ok, "비밀번호를 성공적으로 수정하였습니다.", null);
        } catch (Exception e) {
            log.error("Failed to update password: " + e.getMessage());
            return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "Failed to update password");
        }
    }

    // URL에서 S3 키를 추출하는 메서드
    private String extractKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        String bucketPrefix = "https://" + bucketName + ".s3.amazonaws.com/";
        if (url.startsWith(bucketPrefix)) {
            return url.substring(bucketPrefix.length());
        }
        return null;
    }


    // MultipartFile을 압축하는 메서드
    private byte[] compressImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            log.info("이미지 파일이 비어있습니다.");
            return null;
        }
        try {
            // 원본 이미지 읽어오기
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());

            // 이미지 읽기 실패 확인
            if (originalImage == null) {
                log.info("이미지를 읽을 수 없습니다.");
                return null;
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            // 중앙에서 정사각형으로 자르기 위한 좌표 계산
            int x = 0;
            int y = 0;
            int size = Math.min(width, height);

            if (width > height) {
                // 가로가 더 길면 가운데를 기준으로 자르기
                x = (width - height) / 2;
                size = height;
            } else if (height > width) {
                // 세로가 더 길면 가운데를 기준으로 자르기
                y = (height - width) / 2;
                size = width;
            }

            // 중앙 부분 자르기
            BufferedImage croppedImage = originalImage.getSubimage(x, y, size, size);

            // 원하는 크기로 리사이징
            BufferedImage resizedImage = new BufferedImage(profileWidth, profileHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(croppedImage, 0, 0, profileWidth, profileHeight, null);
            g2d.dispose();

            // 결과 이미지를 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.info("압축 이미지 생성 중 오류 발생 : " + e);
            return null;
        }
    }
}
