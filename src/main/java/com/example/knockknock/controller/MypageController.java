package com.example.knockknock.controller;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse listLikeIndie(@PathVariable Long userId, @RequestParam int pageNum, @RequestParam int pageSize){
        return mypageService.listLikeIndie(userId, pageNum, pageSize);
    }
}
