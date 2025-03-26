package com.example.knockknock.controller;

import com.example.knockknock.controller.request.MovieRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ApiResponse likeIndie(@RequestBody MovieRequest.LikeIndieRequest request,
                                 Authentication authentication) {
        return movieService.likeIndie(request, authentication);
    }
}
