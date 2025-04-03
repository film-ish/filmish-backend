package com.example.knockknock.controller;


import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.MakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/makers")
@RequiredArgsConstructor
@Slf4j
public class MakerController {
    private final MakerService makerService;

    @GetMapping("")
    @Operation(summary = "영화인 목록 조회", description = "영화인 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse makerList(@RequestParam(name = "page") int pageNum,
                                @RequestParam(name = "size") int pageSize){
        return makerService.makerList(pageNum, pageSize);
    }

}
