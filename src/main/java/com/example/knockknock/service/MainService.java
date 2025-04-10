package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainService {
    private final ReviewRepository reviewRepository;
    private final IndieMovieRepository indieMovieRepository;
    private final LikeIndieRepository likeIndieRepository;
    private final ReviewImageRepository reviewImageRepository;

    /*
     조회수 기준 베스트 리뷰 3개,
     개봉일 기준 최신 독립 영화 10개,
     좋아요 개수 기준 영화 10개,
     평점 기준 영화 10개
     */
    public ApiResponse mainProcess(CustomUserDetails userDetails){
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        // 1. 조회수 기준 베스트 리뷰 3개
        List<Review> reviews = reviewRepository.findBestReviews(
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "views"))
        );
        List<ReviewResponse.Detail> orderByViews = reviews.stream()
                .map(review -> {
                    List<ReviewImage> images = reviewImageRepository.findByReviewId(review.getId()).orElse(Collections.emptyList());
                    List<ReviewImageResponse.Detail> reviewImages = images.stream()
                            .map(ReviewImageResponse.Detail::of)
                            .collect(Collectors.toList());

                    return ReviewResponse.Detail.of(
                            review,
                            review.getUser().getNickname(),
                            review.getUser().getHeadImage(),
                            reviewImages
                    );
                }).toList();

        // 2. 개봉일 기준 최신 독립 영화 10개
        List<IndieMovie> latest = indieMovieRepository.findLatestMovies(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "pubdate"))
        );

        List<IndieResponse.StillcutDetail> orderByDate = latest.stream()
                .map(indieMovie -> {
                    Boolean like = false;
                    if(userId != null){
                        like = likeIndieRepository.findByIndieMovieIdAndUserId(indieMovie.getId(), userId).isPresent();
                    }

                    // 스틸컷 주소
                    String stillcut = indieMovie.getStillcuts().isEmpty() ? null : indieMovie.getStillcuts().get(0).getStillcut();
                    return IndieResponse.StillcutDetail.of(indieMovie, stillcut, like);
                }).collect(Collectors.toList());

        // 3. 좋아요 개수 기준 영화 10개
        List<IndieMovie> bestMovies = indieMovieRepository.findBestMovies(
                PageRequest.of(0, 10)
        );

        List<IndieResponse.LikeDetail<Float>> orderByLikes = bestMovies.stream()
                .map(indieMovie -> {
                    Boolean like = false;
                    if(userId != null){
                        like = likeIndieRepository.findByIndieMovieIdAndUserId(indieMovie.getId(), userId).isPresent();
                    }

                    String poster = indieMovie.getPosters().isEmpty() ? null : indieMovie.getPosters().get(0).getThumbnail();
                    String stillcut = indieMovie.getStillcuts().isEmpty() ? null : indieMovie.getStillcuts().get(0).getStillcut();
                    List<String> genres = indieMovie.getGenres().stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .collect(Collectors.toList());
                    return IndieResponse.LikeDetail.from(indieMovie, poster, stillcut, genres, like);
                }).collect(Collectors.toList());

        /*
            sorted() -> 람다식 사용
            - 결과가 양수이면, b가 앞에 위치 (내림차순)
            - 결과가 음수이면, a가 앞에 위치
         */

        // 4. 평점 기준 영화 10개
        List<IndieMovie> ratingMovies = indieMovieRepository.findRankedMovies(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "averageRating"))
        );

        List<IndieResponse.LikeDetail<Float>> orderByAvg = ratingMovies.stream()
                .map(indieMovie -> {
                    Boolean like = false;
                    if(userId != null){
                        like = likeIndieRepository.findByIndieMovieIdAndUserId(indieMovie.getId(), userId).isPresent();
                    }
                    String poster = indieMovie.getPosters().isEmpty() ? null : indieMovie.getPosters().get(0).getThumbnail();
                    String stillcut = indieMovie.getStillcuts().isEmpty() ? null : indieMovie.getStillcuts().get(0).getStillcut();
                    List<String> genres = indieMovie.getGenres().stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .collect(Collectors.toList());
                    return IndieResponse.LikeDetail.from(indieMovie, poster, stillcut, genres, like);
                }).toList();

        MainResponse.AllList<Float, Float> mainResponse = MainResponse.AllList.of(orderByViews, orderByDate, orderByLikes, orderByAvg);
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회하였습니다.", mainResponse);
    }
}
