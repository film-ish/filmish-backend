package com.example.knockknock.controller;

import com.example.knockknock.controller.request.ReviewRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "영화 리뷰 등록", description = "영화 리뷰를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse writeReview(@ModelAttribute ReviewRequest.Write request, Authentication authentication){
        return reviewService.writeReview(request, authentication);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "영화 리뷰 수정", description = "영화 리뷰를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정함")
    })
    public ApiResponse updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest.Update request,
                                    Authentication authentication){
        return reviewService.updateReview(request, reviewId, authentication);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "영화 리뷰 상세 조회", description = "영화 리뷰를 상세 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse detailReview(@PathVariable Long reviewId){
        return reviewService.detailReview(reviewId);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "영화 리뷰 삭제", description = "영화 리뷰를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제함")
    })
    public ApiResponse deleteReview(@PathVariable Long reviewId, Authentication authentication){
        return reviewService.deleteReview(reviewId, authentication);
    }

    @PostMapping("/comments")
    @Operation(summary = "영화 리뷰 댓글 등록", description = "영화 리뷰 댓글을 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse writeComment(@RequestBody ReviewRequest.WriteComment request, Authentication authentication){
        return reviewService.writeComment(request, authentication);
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "영화 리뷰 댓글 수정", description = "영화 리뷰 댓글을 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정함")
    })
    public ApiResponse updateComment(@PathVariable Long commentId, @RequestBody ReviewRequest.UpdateComment request,
                                     Authentication authentication){
        return reviewService.updateComment(request, commentId, authentication);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "영화 리뷰 댓글 삭제", description = "영화 리뷰 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제함")
    })
    public ApiResponse deleteComment(@PathVariable Long commentId, Authentication authentication){
        return reviewService.deleteComment(commentId, authentication);
    }

    @GetMapping("/{reviewId}/comments")
    @Operation(summary = "영화 리뷰 댓글 목록 조회", description = "영화 리뷰 댓글 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse commentList(@PathVariable Long reviewId,
                                   @RequestParam(name = "page") int pageNum,
                                   @RequestParam(name = "size") int pageSize){
        return reviewService.commentList(reviewId, pageNum, pageSize);
    }


}
