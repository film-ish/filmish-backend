package com.example.knockknock.controller;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/recommendation")
public class RecommendController {
    private final RecommendService recommendService;

    @GetMapping
    @Operation(summary = "추천 목록 조회", description = "추천 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse mainProcess(@RequestParam Integer num, @AuthenticationPrincipal CustomUserDetails userDetails){
        return recommendService.recommendProcess(num, userDetails);
    }

    @GetMapping("/genres")
    @Operation(summary = "장르 목록 조회", description = "장르 목록 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listGenre(@RequestParam int pageNum, @RequestParam int pageSize){
        return recommendService.listGenre(pageNum, pageSize);
    }

    @GetMapping("/rates")
    @Operation(summary = "평점별 목록 조회", description = "평점별 목록 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listRate(@RequestParam Integer num, @RequestParam double minValue, @RequestParam double maxValue, @RequestParam int pageNum, @RequestParam int pageSize, @AuthenticationPrincipal CustomUserDetails userDetails){
        ApiResponse recommendResult = recommendService.recommendProcess(num, userDetails);
        return recommendService.listRate(minValue, maxValue, pageNum, pageSize, recommendResult);
    }
}
