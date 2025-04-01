package com.example.knockknock.controller;

import com.example.knockknock.controller.request.RateRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.RateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
@Slf4j
public class RateController {
    private final RateService rateService;

    @PostMapping(value="", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "영화 평점 등록", description = "영화 평점을 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse createRate(@ModelAttribute RateRequest.Create request, Authentication authentication){
        return rateService.createRate(request, authentication);
    }

    @GetMapping("/{rateId}")
    @Operation(summary = "영화 평점 상세 조회", description = "영화 평점을 상세 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회됨")
    })
    public ApiResponse detailRate(@PathVariable Long rateId) {
        return rateService.detailRate(rateId);
    }

    @PutMapping("/{rateId}")
    @Operation(summary = "영화 평점 수정", description = "영화 평점을 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정함")
    })
    public ApiResponse updateRate(@PathVariable Long rateId, @RequestBody RateRequest.Update request){
        return rateService.updateRate(request, rateId);
    }
}
