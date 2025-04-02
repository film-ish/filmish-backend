package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    @Value("${fastapi.url}")
    private String fastApiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

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
}
