package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.GenreResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.Genre;
import com.example.knockknock.entity.Poster;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.GenreRepository;
import com.example.knockknock.repository.IndieGenreRepository;
import com.example.knockknock.repository.PosterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
}
