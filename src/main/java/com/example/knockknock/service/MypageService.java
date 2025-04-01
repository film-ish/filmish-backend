package com.example.knockknock.service;

import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    private final GenreRepository genreRepository;
    private final RateRepository rateRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;

    public ApiResponse listLikeIndie(Long userId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<IndieResponse.LikeDetail> likePage = likeIndieRepository.findByUserId(userId, pageable)
                .map(likeIndie -> {
                    IndieMovie movie = likeIndie.getIndieMovie();
                    List<Poster> posters = posterRepository.findByIndieId(movie.getId());
                    List<IndieGenre> indieGenres = indieGenreRepository.findByIndieId(movie.getId());

                    List<String> categories = indieGenres
                            .stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .toList();

                    String posterUrl = posters.isEmpty() ? null : posters.get(0).getPoster();
                    return IndieResponse.LikeDetail.of(movie, posterUrl, categories);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", likePage);
    }

    public ApiResponse listRating(Long userId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<MypageResponse.RateDetail> ratePage = rateRepository.findByUserId(userId, pageable)
                .map(rate -> {
                    IndieMovie movie = rate.getIndieMovie();
                    Poster poster = posterRepository.findByIndieId(movie.getId()).get(0);

                    return MypageResponse.RateDetail.of(rate, movie, poster.getPoster());
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", ratePage);
    }

    public ApiResponse listReviews(Long userId, int pageNum, int pageSize){
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

    public ApiResponse listQnas(Long userId, int pageNum, int pageSize){
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
}
