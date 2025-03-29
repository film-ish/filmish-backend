package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.ReviewRequest;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final IndieMovieRepository indieMovieRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final MakerMovieRepository makerMovieRepository;

    private final S3Service s3Service;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    
    public ApiResponse reviewList(Long indieId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewResponse.Detail> reviewPage = reviewRepository.findByIndieId(indieId, pageable)
                .map(review -> {
                    User writer = review.getUser();

                    List<ReviewImage> imageList = null;
                    Optional<List<ReviewImage>> images = reviewImageRepository.findByReviewId(review.getId());
                    if (!images.isEmpty()){
                        imageList = images.get();
                    }
                    return ReviewResponse.Detail.of(
                            review,
                            writer.getNickname(),
                            writer.getHeadImage(),
                            imageList.stream()
                                    .map(ReviewImageResponse.Detail::of)
                                    .toList()
                    );
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", reviewPage);
    }
    
    public ApiResponse writeReview(ReviewRequest.Write request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).get();
        IndieMovie indieMovie = indieMovieRepository.findById(request.getIndieId()).get();

        Review newReview = Review.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .views(0)
                .user(user)
                .indieMovie(indieMovie)
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
        return ApiSuccessResponse.response(ResponseCode.Created, "리뷰가 성공적으로 등록되었습니다.", null);
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
                    .path(imagePath)
                    .review(review)
                    .build();

            return reviewImage;
        } catch (IOException e) {
            log.info("이미지 등록에 실패했습니다.");
            return null;
        }
    }

    public ApiResponse updateReview(ReviewRequest.Update request, Long reviewId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Review review = reviewRepository.findById(reviewId).get();
        log.info("review = " + review.getTitle());

        if (review.getUser().getId() != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "수정 권한이 없습니다.");
        }
        review.setTitle(request.getTitle());
        log.info("title = " + request.getTitle());
        log.info("content = " + request.getContent());
        review.setContent(request.getContent());

        reviewRepository.save(review);

        return ApiSuccessResponse.response(ResponseCode.Ok, "리뷰 수정이 완료되었습니다.", null);
    }

    public ApiResponse detailReview(Long reviewId){
        log.info("입력된 reviewId = " + reviewId);
        Optional<Review> selectReview = reviewRepository.findById(reviewId);
        Review savedReview = null;
        if(!selectReview.isEmpty()) {
            Review review = selectReview.get();
            review.setViews(review.getViews() + 1);
            savedReview = reviewRepository.save(review);
        }

        List<ReviewImageResponse.Detail> images = null;
        Optional<List<ReviewImage>> imageList = reviewImageRepository.findByReviewId(reviewId);
        ReviewResponse.Detail reviewResponse = null;

        if(!imageList.isEmpty()){
            List<ReviewImage> allImages = imageList.get();
            images = allImages.stream()
                        .map(image -> {
                            return ReviewImageResponse.Detail.of(image);
                        })
                        .collect(Collectors.toList());
            reviewResponse = ReviewResponse.Detail.of(savedReview, savedReview.getUser().getNickname(), savedReview.getUser().getHeadImage(), images);
        }
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 리뷰를 성공적으로 조회했습니다.", reviewResponse);
    }

    public ApiResponse deleteReview(Long reviewId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Review review = reviewRepository.findById(reviewId).get();

        if(userDetails.getUserId() != review.getUser().getId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "삭제 권한이 없습니다.");
        }

        review.deleteSoftly(Instant.now());
        reviewRepository.save(review);
        return ApiSuccessResponse.response(ResponseCode.Ok, "게시물이 삭제되었습니다.", null);
    }

    public ApiResponse writeComment(ReviewRequest.WriteComment request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User writer = userRepository.findById(userDetails.getUserId()).get();
        Review review = reviewRepository.findById(request.getReviewId()).get();

        // ParentId가 null인 경우 (댓글)
        if (request.getParentId() == null) {
            ReviewComment newComment = ReviewComment.builder()
                    .content(request.getContent())
                    .user(writer)
                    .review(review)
                    .createdAt(Instant.now())
                    .parentComment(null)
                    .build();
            reviewCommentRepository.save(newComment);
            return ApiSuccessResponse.response(ResponseCode.Created, "댓글이 성공적으로 등록되었습니다.", null);
        } else {        // ParentId가 존재하는 경우 (대댓글)
            ReviewComment parentComment = reviewCommentRepository.findById(request.getParentId()).get();
            ReviewComment newComment = ReviewComment.builder()
                    .content(request.getContent())
                    .user(writer)
                    .review(review)
                    .createdAt(Instant.now())
                    .parentComment(parentComment)
                    .build();
            reviewCommentRepository.save(newComment);
            return ApiSuccessResponse.response(ResponseCode.Created, "댓글이 성공적으로 등록되었습니다.", null);
        }
    }

    public ApiResponse updateComment(ReviewRequest.UpdateComment request, Long commentId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ReviewComment reviewComment = reviewCommentRepository.findById(commentId).get();

        if(userDetails.getUserId() != reviewComment.getUser().getId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "수정 권한이 없습니다.");
        }

        if (request.getContent() == null){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "입력된 내용이 없습니다.");
        }

        reviewComment.setContent(request.getContent());
        reviewComment.setUpdatedAt(Instant.now());
        reviewCommentRepository.save(reviewComment);
        return ApiSuccessResponse.response(ResponseCode.Ok, "댓글이 성공적으로 수정되었습니다.", null);
    }

    public ApiResponse deleteComment(Long commentId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ReviewComment reviewComment = reviewCommentRepository.findById(commentId).get();

        if(userDetails.getUserId() != reviewComment.getUser().getId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "삭제 권한이 없습니다.");
        }

        reviewComment.deleteSoftly(Instant.now());
        reviewCommentRepository.save(reviewComment);
        return ApiSuccessResponse.response(ResponseCode.Ok, "댓글이 성공적으로 삭제되었습니다.", null);
    }

    public ApiResponse listComment(Long reviewId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        Long indieId = reviewRepository.findById(reviewId).get().getIndieMovie().getId();
        Page<ReviewCommentResponse.Detail> commentList = reviewCommentRepository.findByReviewId(reviewId, pageable)
                .map(reviewComment -> {
                    if(reviewComment.getParentComment() != null) return null;    // null이 아니라 pass 했으면 좋겠음
                    User writer = reviewComment.getUser();
                    Optional<MakerMovie> makerMovie = makerMovieRepository.findByMakerIdAndUserId(writer.getId(), indieId);
                    MakerMovie makerMovie1 = null;
                    if(!makerMovie.isEmpty()) {
                        makerMovie1 = makerMovie.get();
                    }

                    List<ReviewComment> subList = null;
                    List<ReviewCommentResponse.Detail> subCommentList = null;
                    Optional<List<ReviewComment>> subComments = reviewCommentRepository.findByParentCommentId(reviewComment.getId());
                    if(!subComments.isEmpty()){
                        subList = subComments.get();
                        subCommentList = subList.stream()
                                .map(subComment -> {
                                    User subCommentWriter = subComment.getUser();
                                    Optional<MakerMovie> subMakerMovie = makerMovieRepository
                                            .findByMakerIdAndUserId(subCommentWriter.getId(), indieId);

                                    return ReviewCommentResponse.Detail.of(
                                            subComment,
                                            subCommentWriter,
                                            subMakerMovie.orElse(null),
                                            null
                                    );
                                }).toList();
                    }
                    return ReviewCommentResponse.Detail.of(reviewComment, writer, makerMovie1, subCommentList);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "댓글 목록을 성공적으로 조회하였습니다.", commentList);
    }
}
