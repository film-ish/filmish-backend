package com.example.knockknock.controller;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.controller.response.SearchResponse;
import com.example.knockknock.service.SearchService;
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
@RequestMapping("/knockknock")
@Slf4j
public class SearchController {
    private final SearchService searchService;

    @GetMapping("")
    @Operation(summary = "통합 검색", description = "통합 검색을 실시합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse totalSearch(@RequestParam("data") String query, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("입력된 query ={}", query);
        return searchService.totalSearch(query, 0, userDetails);
    }

    @GetMapping("/movies")
    @Operation(summary = "영화 검색", description = "영화 검색을 실시합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse movieSearch(@RequestParam("data") String query, @RequestParam("page") int pageNum, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("입력된 query ={}", query);
        List<SearchResponse.MovieDetail> result =  searchService.movieSearch(query, pageNum, userDetails);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 검색이 완료되었습니다.", result);
    }

    @GetMapping("/actors")
    @Operation(summary = "배우 검색", description = "배우 검색을 실시합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse actorSearch(@RequestParam("data") String query, @RequestParam("page") int pageNum, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("입력된 query ={}", query);
        List<SearchResponse.MakerDetail> result = searchService.actorSearch(query, pageNum, userDetails);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 검색이 완료되었습니다.", result);
    }

    @GetMapping("/directors")
    @Operation(summary = "감독 검색", description = "감독 검색을 실시합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse directorSearch(@RequestParam("data") String query, @RequestParam("page") int pageNum, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("입력된 query ={}", query);
        List<SearchResponse.MakerDetail> result =  searchService.directorSearch(query, pageNum, userDetails);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 검색이 완료되었습니다.", result);
    }

    @GetMapping("/genres")
    @Operation(summary = "장르 검색", description = "장르 검색을 실시합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse genreSearch(@RequestParam("data") String query, @RequestParam("page") int pageNum, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("입력된 query ={}", query);
        List<SearchResponse.KeyMovies> result = searchService.genreSearch(query, pageNum, userDetails);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 검색이 완료되었습니다.", result);
    }

    @GetMapping("/keywords")
    @Operation(summary = "키워드 검색", description = "키워드 검색을 실시합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse keywordSearch(@RequestParam("data") String query, @RequestParam("page") int pageNum, @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("입력된 query ={}", query);
        List<SearchResponse.KeyMovies> result =  searchService.keywordSearch(query, pageNum, userDetails);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 검색이 완료되었습니다.", result);
    }


}
