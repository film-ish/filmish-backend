package com.example.knockknock.controller;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class MypageController {
    private final MypageService mypageService;

    @GetMapping("/likes")
    @Operation(summary = "내 보고싶어요 목록 조회", description = "나의 보고싶어요 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listLikeIndie(@PathVariable Long userId,
                                     @RequestParam int pageNum,
                                     @RequestParam int pageSize,
                                     @AuthenticationPrincipal CustomUserDetails userDetails){
        return mypageService.listLikeIndie(userId, pageNum, pageSize, userDetails);
    }

    @GetMapping("/ratings")
    @Operation(summary = "내 평점 목록 조회", description = "나의 평점 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listRating(@PathVariable Long userId,
                                  @RequestParam int pageNum,
                                  @RequestParam int pageSize,
                                  @AuthenticationPrincipal CustomUserDetails userDetails){
        return mypageService.listRating(userId, pageNum, pageSize, userDetails);
    }

    @GetMapping("/reviews")
    @Operation(summary = "내 리뷰 목록 조회", description = "나의 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listReviews(@PathVariable Long userId,
                                   @RequestParam int pageNum,
                                   @RequestParam int pageSize,
                                   @AuthenticationPrincipal CustomUserDetails userDetails){
        return mypageService.listReviews(userId, pageNum, pageSize, userDetails);
    }

    @GetMapping("/qna")
    @Operation(summary = "내 Qna 목록 조회", description = "나의 Qna 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listQnas(@PathVariable Long userId,
                                @RequestParam int pageNum,
                                @RequestParam int pageSize,
                                @AuthenticationPrincipal CustomUserDetails userDetails){
        return mypageService.listQnas(userId, pageNum, pageSize, userDetails);
    }

    @GetMapping("/reviews/comments")
    @Operation(summary = "내 댓글(리뷰) 목록 조회", description = "나의 댓글 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listReviewComments(@PathVariable Long userId,
                                          @RequestParam int pageNum,
                                          @RequestParam int pageSize,
                                          @AuthenticationPrincipal CustomUserDetails userDetails){
        return mypageService.listReviewComments(userId, pageNum, pageSize, userDetails);
    }

    @GetMapping("/qna/comments")
    @Operation(summary = "내 댓글(QnA) 목록 조회", description = "나의 댓글 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listQnaComments(@PathVariable Long userId,
                                       @RequestParam int pageNum,
                                       @RequestParam int pageSize,
                                       @AuthenticationPrincipal CustomUserDetails userDetails){
        return mypageService.listQnaComments(userId, pageNum, pageSize, userDetails);
    }
}
