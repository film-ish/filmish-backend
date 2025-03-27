package com.example.knockknock.controller;

import com.example.knockknock.controller.request.MovieRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {
    private final MovieService movieService;

    @PostMapping("/likes")
    @Operation(summary = "보고싶어요 등록", description = "독립 영화 보고싶어요를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse likeIndie(@RequestBody MovieRequest.LikeIndie request,
                                 Authentication authentication) {
        return movieService.likeIndie(request, authentication);
    }

    @DeleteMapping("/likes/{likeId}")
    @Operation(summary = "보고싶어요 삭제", description = "독립 영화 보고싶어요를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제함")
    })
    public ApiResponse unlikeIndie(@PathVariable Long likeId){
        return movieService.unlikeIndie(likeId);
    }

    @GetMapping("/{movieId}")
    @Operation(summary = "영화 상세 정보 조회", description = "영화 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse movieInfo(@PathVariable Long movieId){
        return movieService.movieInfo(movieId);
    }

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "영화 리뷰 등록", description = "영화 리뷰를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse writeReview(@ModelAttribute MovieRequest.WriteReview request, Authentication authentication){
        return movieService.writeReview(request, authentication);
    }
}
