package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final LikeIndieRepository likeIndieRepository;
    private final IndieMovieRepository indieMovieRepository;
    private final RateRepository rateRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    public ApiResponse listLikeIndie(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<IndieResponse.LikeDetail> likePage = likeIndieRepository.findByUserId(userId, pageable)
                .map(likeIndie -> {
                    IndieMovie indieMovie = likeIndie.getIndieMovie();
                    String poster = indieMovie.getPosters().isEmpty() ? null : indieMovie.getPosters().get(0).getThumbnail();
                    String stillcut = indieMovie.getStillcuts().isEmpty() ? null : indieMovie.getStillcuts().get(0).getStillcut();
                    List<String> genres = indieMovie.getGenres().stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .collect(Collectors.toList());

                    return IndieResponse.LikeDetail.from(indieMovie, poster, stillcut, genres, true);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", likePage);
    }

    public ApiResponse listRating(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<MypageResponse.RateDetail> ratePage = rateRepository.findByUserId(userId, pageable)
                .map(rate -> {
                    IndieMovie indieMovie = rate.getIndieMovie();
                    String poster = indieMovie.getPosters().isEmpty() ? null : indieMovie.getPosters().get(0).getThumbnail();
                    return MypageResponse.RateDetail.of(rate, indieMovie, poster);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", ratePage);
    }

    public ApiResponse listReviews(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<MypageResponse.ReviewDetail> reviewPage = reviewRepository.findByUserId(userId, pageable)
                .map(review -> {
                    IndieMovie movie = review.getIndieMovie();
                    Optional<List<ReviewImage>> imageList = reviewImageRepository.findByReviewId(review.getId());
                    List<String> images = imageList.isEmpty() ? null : imageList.get().stream()
                            .map(ReviewImage::getPath).toList();

                    return MypageResponse.ReviewDetail.of(review, movie, images);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", reviewPage);
    }

    public ApiResponse listQnas(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<MypageResponse.QnaDetail> qnaPage = qnaRepository.findByUserId(userId, pageable)
                .map(qna -> {
                    Maker maker = qna.getMaker();
                    Optional<List<QnaComment>> commentList = qnaCommentRepository.findByQnaId(qna.getId());
                    List<QnaCommentResponse.Detail> comments = commentList.isEmpty() ? null : commentList.get().stream()
                            .map(qnaComment -> {
                                Optional<List<QnaComment>> subcommentList = qnaCommentRepository.findByParentCommentId(qnaComment.getId());
                                List<QnaCommentResponse.Detail> subcomments = subcommentList.isEmpty() ? null : subcommentList.get().stream()
                                        .map(subcomment -> {
                                            return QnaCommentResponse.Detail.of(subcomment, subcomment.getUser(), null);
                                        }).toList();
                                return QnaCommentResponse.Detail.of(qnaComment, qnaComment.getUser(), subcomments);
                            }).toList();
                    return MypageResponse.QnaDetail.of(qna, maker, comments);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", qnaPage);
    }

    public ApiResponse listReviewComments(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<MypageResponse.CommentReviewDetail> reviewPage = reviewCommentRepository.findByUserId(userId, pageable)
                .map(reviewComment -> {
                    Review review = reviewRepository.findById(reviewComment.getReview().getId()).get();
                    IndieMovie movie = indieMovieRepository.findById(review.getIndieMovie().getId()).get();

                    Optional<List<ReviewImage>> imageList = reviewImageRepository.findByReviewId(review.getId());
                    List<ReviewImageResponse.Detail> images = imageList.isEmpty() ? null : imageList.get().stream()
                            .map(reviewImage -> {
                                return ReviewImageResponse.Detail.of(reviewImage);
                            }).toList();

                    return MypageResponse.CommentReviewDetail.of(review, movie, images);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", reviewPage);
    }

    public ApiResponse listQnaComments(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Optional<List<QnaComment>> replyList = qnaCommentRepository.findByUserId(userId);
        List<QnaComment> replies = replyList.isEmpty() ? null : replyList.get();
        List<Long> replyIds = replies.stream()
                .map(QnaComment::getId).toList();
        Page<QnaResponse.Detail> qnaPage = qnaRepository.findDistinctQnasByCommentIds(replyIds, pageable)
                .map(qna -> {
                    List<QnaComment> commentList = qnaCommentRepository.findByQnaId(qna.getId()).get();
                    List<QnaCommentResponse.Detail> comments = commentList.stream()
                            .map(qnaComment -> {
                                Optional<List<QnaComment>> subCommentList = qnaCommentRepository.findByParentCommentId(qnaComment.getId());
                                List<QnaCommentResponse.Detail> subComments = subCommentList.isEmpty() ? null : subCommentList.get().stream()
                                        .map(subComment -> {
                                            return QnaCommentResponse.Detail.of(subComment, subComment.getUser(), null);
                                        }).toList();
                                return QnaCommentResponse.Detail.of(qnaComment, qnaComment.getUser(), subComments);
                            }).toList();
                    return QnaResponse.Detail.of(qna, qna.getUser(), comments);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, " 성공적으로 조회되었습니다.", qnaPage);
    }
}
