package com.example.knockknock.controller;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.MovieRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.MovieService;
import com.example.knockknock.service.RateService;
import com.example.knockknock.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {
    private final MovieService movieService;
    private final ReviewService reviewService;
    private final RateService rateService;

    @PostMapping("/likes")
    @Operation(summary = "보고싶어요 등록", description = "독립 영화 보고싶어요를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse likeIndie(@RequestBody MovieRequest.LikeIndie request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return movieService.likeIndie(request, userDetails);
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
        return movieService.movieDetail(movieId);
    }

    @GetMapping("{movieId}/reviews")
    @Operation(summary = "영화 리뷰 목록 조회", description = "영화 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse reviewList(@PathVariable Long movieId,
                                  @RequestParam(name = "page") int pageNum,
                                  @RequestParam(name = "size") int pageSize){
        return reviewService.reviewList(movieId, pageNum, pageSize);
    }

    @GetMapping("/like-commercial")
    @Operation(summary = "상업 영화 목록 조회", description = "상업 영화 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listCommercial(){
        return movieService.listCommercial();
    }

    @PostMapping("/like-commercial")
    @Operation(summary = "상업 영화 좋아요 등록", description = "상업 영화 좋아요를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse likeCommercial(@RequestBody MovieRequest.LikeCommercial request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return movieService.likeCommercial(request, userDetails);
    }

    @GetMapping("/genre/{genreId}")
    @Operation(summary = "장르별 전체 영화 목록 조회", description = "장르별 전체 영화 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse genreMovies(@PathVariable Long genreId,
                                   @RequestParam(name = "page") int pageNum,
                                   @RequestParam(name = "size") int pageSize) {
        return movieService.genreMovies(genreId, pageNum, pageSize);
    }

    @GetMapping("/like-commercials")
    @Operation(summary = "상업 영화 좋아요 입력 여부 확인", description = "상업 영화 좋아요 입력 여부를 확인합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse checkLikeCommercial(@AuthenticationPrincipal CustomUserDetails userDetails){
        return movieService.checkLikeCommercial(userDetails);
    }


    @GetMapping("{movieId}/ratings")
    @Operation(summary = "영화 평점 목록 조회", description = "영화 평점 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse rateList(@PathVariable Long movieId,
                                @RequestParam(name =    "page") int pageNum,
                                @RequestParam(name = "size") int pageSize){
        return rateService.rateList(movieId, pageNum, pageSize);
    }
}
