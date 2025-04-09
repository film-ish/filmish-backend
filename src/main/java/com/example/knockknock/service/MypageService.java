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

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final LikeIndieRepository likeIndieRepository;
    private final IndieMovieRepository indieMovieRepository;
    private final PosterRepository posterRepository;
    private final IndieGenreRepository indieGenreRepository;
    private final RateRepository rateRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final StillcutRepository stillcutRepository;

    public ApiResponse listLikeIndie(Long userId, int pageNum, int pageSize, CustomUserDetails userDetails) {
        if (userId != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<IndieResponse.LikeDetail> likePage = likeIndieRepository.findByUserId(userId, pageable)
                .map(likeIndie -> {
                    IndieMovie movie = likeIndie.getIndieMovie();
                    Long movieId = movie.getId();
                    List<Poster> posters = posterRepository.findByIndieId(movieId).orElse(Collections.emptyList());
                    List<IndieGenre> indieGenres = indieGenreRepository.findByIndieId(movieId).orElse(Collections.emptyList());

                    List<String> categories = indieGenres
                            .stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .toList();

                    String posterUrl = posters.isEmpty() ? null : posters.get(0).getPoster();

                    // 스틸컷 주소
                    List<Stillcut> stillcuts = stillcutRepository.findByIndieId(movie.getId()).orElse(Collections.emptyList());
                    String stillcut = null;
                    if(!stillcuts.isEmpty()){
                        stillcut = stillcuts.get(0).getStillcut();
                    }

                    // 평점 계산
                    List<Rate> rates = rateRepository.findAllByIndieId(movieId)
                            .orElse(Collections.emptyList());

                    float average = (float) rates.stream()
                            .mapToDouble(Rate::getValue) // Rate 객체에서 평균값 추출
                            .average()                   // 평균 계산 (OptionalDouble 반환)
                            .orElse(0.0);         // 평균 값 없으면 0.0 반환

                    return IndieResponse.LikeDetail.of(movie, posterUrl, stillcut, average, categories, true);
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
                    IndieMovie movie = rate.getIndieMovie();
                    String poster = null;
                    List<Poster> posters = posterRepository.findByIndieId(movie.getId()).orElse(Collections.emptyList());
                    if (!posters.isEmpty()){
                        poster = posters.get(0).getPoster();
                    }
                    return MypageResponse.RateDetail.of(rate, movie, poster);
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
                            .map(reviewImage -> {
                                return reviewImage.getPath();
                            }).toList();

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
                .map(qnaComment -> {
                    return qnaComment.getId();
                }).toList();
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
