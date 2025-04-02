package com.example.knockknock.service;

import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserRepository userRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final RateRepository rateRepository;
    private final StillcutRepository stillcutRepository;
    private final PosterRepository posterRepository;
    private final IndieGenreRepository indieGenreRepository;

    /*
     조회수 기준 베스트 리뷰 3개,
     개봉일 기준 최신 독립 영화 10개,
     개인 맞춤 추천 영화 10개,
     좋아요 개수 기준 영화 10개,
     평점 기준 영화 10개
     */
    public ApiResponse mainProcess(){
        // 2. 조회수 기준 베스트 리뷰 3개
        List<Review> SortByViews = reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "views"))
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        List<ReviewResponse.Detail> orderByViews = SortByViews.stream()
                .map(review -> {
                    Optional<User> userEntity = userRepository.findById(review.getUser().getId());
                    //TODO
                    // 만약 작성자가 조회되지 않는 상황이라면 예외 처리를 할 것인가, 말 것인가?
                    User writer = userEntity.isEmpty() ? null : userEntity.get();
                    Optional<List<ReviewImage>> reviewImageList = reviewImageRepository.findByReviewId(review.getId());
                    List<ReviewImage> reviewImages = reviewImageList.isEmpty() ? null : reviewImageList.get();

                    List<ReviewImageResponse.Detail> images = null;
                    if(reviewImages != null){
                        images = reviewImages.stream()
                                .map(reviewImage -> {
                                    return ReviewImageResponse.Detail.of(reviewImage);
                                }).toList();
                    }

                    return ReviewResponse.Detail.of(review, writer.getNickname(), writer.getHeadImage(), images);
                }).toList();

        // 3. 개봉일 기준 최신 독립 영화 10개
        List<IndieMovie> latest = indieMovieRepository.findAll(Sort.by(Sort.Direction.ASC, "pubdate"))
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        List<IndieResponse.Approximate> orderByDate = latest.stream()
                .map(indieMovie -> {
                    // 평점 계산
                    List<Rate> rates = rateRepository.findByIndieId(indieMovie.getId())
                            .orElse(Collections.emptyList());

                    float average = (float) rates.stream()
                            .mapToDouble(Rate::getValue) // Rate 객체에서 평균값 추출
                            .average()
                            .orElse(0.0);         // 평균 값 없으면 0.0 반환

                    // 스틸컷 주소
                    List<Stillcut> stillcuts = stillcutRepository.findByIndieId(indieMovie.getId()).orElse(Collections.emptyList());
                    String stillcut = null;
                    if(!stillcuts.isEmpty()){
                        stillcut = stillcuts.get(0).getStillcut();
                    }
                    return IndieResponse.Approximate.of(indieMovie, average, stillcut);
                }).toList();

        // 4. 좋아요 개수 기준 영화 10개
        List<IndieMovie> allMovies = indieMovieRepository.findAll();
        List<Pair<IndieMovie, Integer>> likeMovies = allMovies.stream()
                .map(indieMovie -> {
                    List<LikeIndie> likeMovie = likeIndieRepository.findByIndieId(indieMovie.getId())
                            .orElse(Collections.emptyList());
                    return Pair.of(indieMovie, likeMovie.size());
                })
                .sorted((a, b) -> b.getRight() - a.getRight()) // 좋아요 개수 기준 내림차순 정렬
                .limit(10)
                .toList();

        List<IndieResponse.LikeDetail> orderByLikes = likeMovies.stream()
                .map(indieMovieIntegerPair -> {
                    IndieMovie movie = indieMovieIntegerPair.getLeft();

                    String image = null;
                    List<Poster> posters = posterRepository.findByIndieId(movie.getId()).orElse(Collections.emptyList());
                    if(posters.isEmpty()){
                        List<Stillcut> stillcuts = stillcutRepository.findByIndieId(movie.getId()).orElse(Collections.emptyList());
                        image = stillcuts.get(0).getStillcut();
                    } else {
                        image = posters.get(0).getPoster();
                    }

                    // 평점 계산
                    List<Rate> rates = rateRepository.findByIndieId(movie.getId())
                            .orElse(Collections.emptyList());

                    float average = (float) rates.stream()
                            .mapToDouble(Rate::getValue) // Rate 객체에서 평균값 추출
                            .average()
                            .orElse(0.0);         // 평균 값 없으면 0.0 반환

                    // 장르 데이터
                    List<IndieGenre> genreList = indieGenreRepository.findByIndieId(movie.getId()).orElse(Collections.emptyList());
                    List<String> genres = genreList.stream()
                            .map(indieGenre -> {
                                return indieGenre.getGenre().getName();
                            }).toList();
                    return IndieResponse.LikeDetail.of(movie, image, average, genres);
                }).toList();

        /*
            sorted() -> 람다식 사용
            - 결과가 양수이면, b가 앞에 위치 (내림차순)
            - 결과가 음수이면, a가 앞에 위치
         */

        // 5. 평점 기준 영화 10개
        List<IndieMovie> ratingMovies = indieMovieRepository.findAll(Sort.by(Sort.Direction.DESC, "averageRating"))
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        List<IndieResponse.LikeDetail> orderByAvg = ratingMovies.stream()
                .map(indieMovie -> {
                    String image = null;
                    List<Poster> posters = posterRepository.findByIndieId(indieMovie.getId()).orElse(Collections.emptyList());
                    if(posters.isEmpty()){
                        List<Stillcut> stillcuts = stillcutRepository.findByIndieId(indieMovie.getId()).orElse(Collections.emptyList());
                        image = stillcuts.get(0).getStillcut();
                    } else {
                        image = posters.get(0).getPoster();
                    }

                    // 평점 계산
                /*
                    List<Rate> rates = rateRepository.findByIndieId(indieMovie.getId())
                            .orElse(Collections.emptyList());

                    float average = (float) rates.stream()
                            .mapToDouble(Rate::getValue) // Rate 객체에서 평균값 추출
                            .average()
                            .orElse(0.0);         // 평균 값 없으면 0.0 반환
                */

                    // 장르 데이터
                    List<IndieGenre> genreList = indieGenreRepository.findByIndieId(indieMovie.getId()).orElse(Collections.emptyList());
                    List<String> genres = genreList.stream()
                            .map(indieGenre -> {
                                return indieGenre.getGenre().getName();
                            }).toList();

                    return IndieResponse.LikeDetail.of(indieMovie, image, indieMovie.getAverageRating(), genres);
                }).toList();

        MainResponse.AllList mainResponse = MainResponse.AllList.of(orderByViews, orderByDate, orderByLikes, orderByAvg);
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회하였습니다.", mainResponse);
    }
}
