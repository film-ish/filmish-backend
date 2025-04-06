package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    @Value("${fastapi.url}")
    private String fastApiUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final GenreRepository genreRepository;
    private final IndieGenreRepository indieGenreRepository;
    private final PosterRepository posterRepository;
    private final RateRepository rateRepository;
    private final IndieMovieRepository indieMovieRepository;

    public ApiResponse recommendProcess(int num, CustomUserDetails userDetails){
        JsonNode recommendMovies = null;
        if (userDetails != null){
            Long userId = userDetails.getUserId();
            // FastAPI 서버에 요청 보냄
            String response = restTemplate.getForObject(fastApiUrl +
                    "/?user_id=" + userId +
                    "&num_recommendations=" + num, String.class);

            // JSON 문자열을 JsonNode로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                recommendMovies = objectMapper.readTree(response); // JSON 파싱
            } catch (Exception e) {
                return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "추천 목록 생성 중 오류가 발생하였습니다.");
            }
        }
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 추천되었습니다.", recommendMovies);
    }

    public ApiResponse listGenre(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        // 1. 페이징된 장르 조회
        Page<Genre> genrePage = genreRepository.findAll(pageable);

        // 2. 장르 ID 목록 추출
        List<Long> genreIds = genrePage.getContent().stream()
                .map(Genre::getId)
                .toList();

        // 3. 장르별 첫 번째 포스터 URL 조회 (JPQL Fetch Join 사용)
        Map<Long, String> genrePosterMap = posterRepository.findFirstPosterByGenreIds(genreIds)
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple[0],  // genreId
                        tuple -> tuple[1] != null ? (String) tuple[1] : "" // posterUrl
                ));

        // 4. DTO 변환 (페이징 정보 유지)
        Page<GenreResponse.Detail> resultPage = genrePage.map(genre ->
                GenreResponse.Detail.of(
                        genre,
                        genrePosterMap.getOrDefault(genre.getId(), "")
                )
        );

        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", resultPage);
    }

    @Transactional(readOnly = true)
    public ApiResponse listRate(double minValue, double maxValue, int pageNum, int pageSize, List<Long> recommendedMovieIds) {
        // 1. 페이지네이션 설정
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        // 2. 평균 평점 조회 (영화별)
        Page<Object[]> movieRatings = rateRepository.findMoviesWithAverageRatingAndRecommendation(
                minValue,
                maxValue,
                recommendedMovieIds,
                pageable
        );

        // 3. 영화 ID 추출
        List<Long> movieIds = movieRatings.getContent().stream()
                .map(result -> (Long) result[0])
                .toList();

        // 4. 포스터 조회 (중복 키 처리)
        Map<Long, String> moviePosterMap = posterRepository.findFirstPosterByMovieIds(movieIds)
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple[0],
                        tuple -> tuple[1] != null ? (String) tuple[1] : "default_poster.jpg",
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));

        // 5. 장르 정보 조회 (일괄 처리)
        List<IndieGenre> indieGenres = indieGenreRepository.findByIndieMovieIds(movieIds);

        // 영화별 장르 매핑 (예: {276: ["드라마", "뮤지컬"]})
        Map<Long, List<String>> movieGenresMap = indieGenres.stream()
                .collect(Collectors.groupingBy(
                        ig -> ig.getIndieMovie().getId(),
                        Collectors.mapping(ig -> ig.getGenre().getName(), Collectors.toList())
                ));

        // 6. 응답 데이터 변환 (MovieListByRating 사용)
        List<RateResponse.MovieListByRating> responseList = movieRatings.getContent().stream()
                .map(result -> {
                    Long movieId = (Long) result[0];
                    double averageRating = (Double) result[1];
                    int ratingCount = ((Long) result[2]).intValue();

                    IndieMovie movie = indieMovieRepository.findById(movieId).orElseThrow();
                    String posterUrl = moviePosterMap.getOrDefault(movieId, "default_poster.jpg");

                    // 장르 정보 가져오기
                    List<String> genres = movieGenresMap.getOrDefault(movieId, Collections.emptyList());

                    return RateResponse.MovieListByRating.builder()
                            .movieId(movieId)
                            .title(movie.getTitle())
                            .posterUrl(posterUrl)
                            .averageRating((float) averageRating)
                            .ratingCount(ratingCount)
                            .genre(String.join(", ", genres)) // 쉼표로 구분된 문자열로 변환
                            .pubdate(movie.getPubdate())
                            .build();
                })
                .collect(Collectors.toList());

        // 7. 추천 영화 우선 정렬
        responseList.sort((m1, m2) -> {
            boolean isRecommended1 = recommendedMovieIds.contains(m1.getMovieId());
            boolean isRecommended2 = recommendedMovieIds.contains(m2.getMovieId());
            return Boolean.compare(isRecommended2, isRecommended1); // 추천 영화 먼저
        });

        // 8. 페이지 객체 생성
        Page<RateResponse.MovieListByRating> resultPage = new PageImpl<>(
                responseList.subList(Math.min(pageNum * pageSize, responseList.size()),
                        Math.min((pageNum + 1) * pageSize, responseList.size())),
                pageable,
                responseList.size()
        );

        return ApiSuccessResponse.response(ResponseCode.Ok, "평점별 영화 목록을 성공적으로 조회했습니다.", resultPage);
    }


    public List<Long> getRecommendedMovieIds(Long userId) {
        // FastAPI 서버에 요청 보내기
        String response = restTemplate.getForObject(fastApiUrl +
                "/?user_id=" + userId +
                "&num_recommendations=10", String.class);

        // JSON 문자열을 JsonNode로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode recommendMovies;
        try {
            recommendMovies = objectMapper.readTree(response); // JSON 파싱
        } catch (Exception e) {
            log.error("추천 목록 생성 중 오류가 발생했습니다.", e);
            throw new RuntimeException("추천 목록 생성 중 오류가 발생하였습니다.");
        }

        // 추천 영화 ID 목록 추출
        List<Long> recommendedMovieIds = recommendMovies.findValues("movieId").stream()
                .map(JsonNode::asLong)
                .toList();

        return recommendedMovieIds;
    }


}
