package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.MovieRequest;
import com.example.knockknock.controller.request.ReviewRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.Review;
import com.example.knockknock.entity.ReviewImage;
import com.example.knockknock.entity.User;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.ReviewImageRepository;
import com.example.knockknock.repository.ReviewRepository;
import com.example.knockknock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public ApiResponse writeReview(ReviewRequest.Write request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).get();

        Review newReview = Review.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .views(0)
                .user(user)
                .createdAt(Instant.now())
                .build();

        Review savedReview = reviewRepository.save(newReview);

        if(request.getImages() != null) {
            List<ReviewImage> reviewImages = request.getImages().stream()
                    .map(image -> uploadSingleImage(savedReview, image))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 이미지 업로드 중 실패한 경우 체크
            if (reviewImages.isEmpty()) {
                // 이미지 업로드 실패 시 ApiErrorResponse 반환
                return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "이미지 업로드에 실패했습니다.");
            }

            reviewImageRepository.saveAll(reviewImages);
        }
        return ApiSuccessResponse.response(ResponseCode.Created, "리뷰 등록되었습니다.", null);
    }

    public ReviewImage uploadSingleImage(Review review, MultipartFile image){
        try {
            // 원본 이미지 S3에 업로드
            String originalFileName = image.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String originalKey = "images/review/" + review.getId() + "-" + System.currentTimeMillis() + fileExtension;

            log.info("originalKey 생성 완료");
            s3Service.uploadFile(bucketName, originalKey, image.getBytes(), image.getContentType());
            log.info("이미지 저장 완료");
            String imagePath = "https://" + bucketName + ".s3.amazonaws.com/" + originalKey;

            ReviewImage reviewImage = ReviewImage.builder()
                    .image(imagePath)
                    .review(review)
                    .build();

            return reviewImage;
        } catch (IOException e) {
            log.info("이미지 등록에 실패했습니다.");
            return null;
        }
    }

    public ApiResponse modifyReview(ReviewRequest.Modify request, Long reviewId){
        Review review = reviewRepository.findById(reviewId).get();
        log.info("review = " + review.getTitle());

        if(review.getDeletedAt() != null){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미 삭제된 게시물입니다.");
        }
        review.setTitle(request.getTitle());
        log.info("title = " + request.getTitle());
        log.info("content = " + request.getContent());
        review.setContent(request.getContent());
        review.setViews(review.getViews() + 1);

        reviewRepository.save(review);

        return ApiSuccessResponse.response(ResponseCode.Ok, "게시물 수정이 완료되었습니다.", null);

    }
}
