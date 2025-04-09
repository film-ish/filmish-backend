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

import java.util.*;
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
    private final StillcutRepository stillcutRepository;

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
    public ApiResponse listRate(double minValue, double maxValue, int pageNum, int pageSize, ApiResponse recommendResult) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        // 평점 범위에 해당하는 영화 목록 조회
        Page<Object[]> movieRatings = rateRepository.findMoviesWithAverageRatingBetween(
                minValue, maxValue, pageable);

        // 추천 영화 ID 목록 추출
        List<Long> recommendedMovieIds = extractRecommendedMovieIds(recommendResult);

        // 영화 ID 추출
        List<Long> movieIds = movieRatings.getContent().stream()
                .map(result -> (Long) result[0])
                .toList();

        // 각 영화의 첫 번째 포스터 조회
        Map<Long, String> moviePosterMap = new HashMap<>();
        if (!movieIds.isEmpty()) {
            List<Object[]> posters = posterRepository.findFirstPosterByMovieIds(movieIds);
            for (Object[] poster : posters) {
                Long movieId = (Long) poster[0];
                String posterUrl = (String) poster[1];
                moviePosterMap.put(movieId, posterUrl);
            }
        }

        Map<Long, String> movieStillcutMap = new HashMap<>();
        if (!movieIds.isEmpty()) {
            // 스틸컷을 위한 유사한 쿼리 사용
            List<Object[]> stillcuts = stillcutRepository.findFirstStillcutByMovieIds(movieIds);
            for (Object[] stillcut : stillcuts) {
                Long movieId = (Long) stillcut[0];
                String stillcutUrl = (String) stillcut[1];
                movieStillcutMap.put(movieId, stillcutUrl);
            }
        }

        // 영화 정보 변환
        List<RateResponse.MovieListByRating> responseList = movieRatings.getContent().stream()
                .map(result -> {
                    Long movieId = (Long) result[0];
                    float rating = ((Number) result[1]).floatValue();

                    // 영화 정보 조회
                    Optional<IndieMovie> movieOpt = indieMovieRepository.findById(movieId);
                    if (movieOpt.isEmpty()) {
                        log.error("영화를 찾을 수 없습니다: {}", movieId);
                        return null; // 영화 없으면 null 반환 (나중에 필터링)
                    }

                    IndieMovie movie = movieOpt.get(); // <-- FIX 1: Optional에서 IndieMovie 객체 추출

                    String posterUrl = moviePosterMap.get(movieId);
                    String stillcutUrl = movieStillcutMap.get(movieId);

                    // 포스터 또는 스틸컷 URL 선택 (String 타입)
                    String imageUrl = posterUrl != null ? posterUrl : stillcutUrl;

                    // Poster 객체 생성 로직 제거 (of 메소드가 String을 받는다고 가정)

                    // of 메소드에 IndieMovie 객체와 imageUrl(String) 전달
                    return RateResponse.MovieListByRating.of(movie, rating, imageUrl); // <-- FIX 2: Poster 객체 대신 imageUrl(String) 전달
                })
                .filter(Objects::nonNull) // <-- FIX 3: 영화를 찾지 못해 null이 된 항목 제거
                .collect(Collectors.toList());

        // 추천 영화 우선 정렬
        sortByRecommendation(responseList, recommendedMovieIds);

        // 페이지 객체 생성 (주의: 필터링 후 size가 달라질 수 있으므로 movieRatings.getTotalElements() 사용)
        Page<RateResponse.MovieListByRating> resultPage = new PageImpl<>(
                responseList, pageable, movieRatings.getTotalElements()); // <-- FIX 4: 전체 개수는 필터링 전 기준으로

        return ApiSuccessResponse.response(
                ResponseCode.Ok,
                "평점별 영화 목록을 성공적으로 조회했습니다.",
                resultPage);
    }


    private List<Long> extractRecommendedMovieIds(ApiResponse recommendResult) {
        if (!(recommendResult instanceof ApiSuccessResponse)) {
            return Collections.emptyList();
        }

        Object data = ((ApiSuccessResponse) recommendResult).getData();
        if (!(data instanceof JsonNode)) {
            return Collections.emptyList();
        }

        JsonNode jsonNode = (JsonNode) data;
        JsonNode recommendationsNode = null;

        if (jsonNode.has("recommendations")) {
            recommendationsNode = jsonNode.get("recommendations");
        } else if (jsonNode.has("data") && jsonNode.get("data").has("recommendations")) {
            recommendationsNode = jsonNode.get("data").get("recommendations");
        }

        if (recommendationsNode == null || !recommendationsNode.isArray()) {
            return Collections.emptyList();
        }

        List<Long> movieIds = new ArrayList<>();
        for (JsonNode node : recommendationsNode) {
            if (node.has("id")) {
                movieIds.add(node.get("id").asLong());
            }
        }

        return movieIds;
    }

    private void sortByRecommendation(List<RateResponse.MovieListByRating> movies, List<Long> recommendedIds) {
        if (recommendedIds.isEmpty()) {
            return;
        }

        movies.sort((m1, m2) -> {
            boolean isRecommended1 = recommendedIds.contains(m1.getMovieId());
            boolean isRecommended2 = recommendedIds.contains(m2.getMovieId());

            if (isRecommended1 == isRecommended2) {
                if (isRecommended1) {
                    return Integer.compare(recommendedIds.indexOf(m2.getMovieId()),
                            recommendedIds.indexOf(m1.getMovieId()));
                }
                return 0; // 둘 다 추천 영화가 아니면 순서 유지
            }
            return isRecommended2 ? 1 : -1; // 추천 영화 먼저
        });
    }


}
