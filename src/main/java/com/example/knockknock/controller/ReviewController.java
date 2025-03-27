package com.example.knockknock.controller;

import com.example.knockknock.controller.request.ReviewRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse modifyReview(@PathVariable Long reviewId, @RequestBody ReviewRequest.Modify request){
        return reviewService.modifyReview(request, reviewId);
    }
}
