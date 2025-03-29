package com.example.knockknock.controller;

import com.example.knockknock.controller.request.QnaRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
@Slf4j
public class QnaController {
    private final QnaService qnaService;

    @PostMapping("")
    @Operation(summary = "QnA 등록", description = "QnA를 등록합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 등록함")
    })
    public ApiResponse writeQna(@RequestBody QnaRequest.WriteQna request, Authentication authentication){
        return qnaService.writeQna(request, authentication);
    }

    @PutMapping("/{qnaId}")
    @Operation(summary = "QnA 수정", description = "QnA를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정함")
    })
    public ApiResponse updateQna(@PathVariable Long qnaId, @RequestBody QnaRequest.Update request, Authentication authentication){
        return qnaService.updateQna(qnaId, request, authentication);
    }

    @DeleteMapping("/{qnaId}")
    @Operation(summary = "QnA 삭제", description = "QnA를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 삭제함")
    })
    public ApiResponse deleteQna(@PathVariable Long qnaId, Authentication authentication){
        return qnaService.deleteQna(qnaId, authentication);
    }

    @GetMapping("/{makerId}")
    @Operation(summary = "QnA 목록 조회", description = "QnA 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse listQna(@PathVariable Long makerId,
                               @RequestParam int pageNum, @RequestParam int pageSize){
        return qnaService.listQna(makerId, pageNum, pageSize);
    }

}
