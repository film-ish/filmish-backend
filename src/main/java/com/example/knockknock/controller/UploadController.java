package com.example.knockknock.controller;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.service.DataUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final DataUploadService dataUploadService;

    @GetMapping("/upload-json")
    public ApiResponse upload(){
        try {
            dataUploadService.uploadJsonFileToElastic();
        }catch(IOException e){
            return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "데이터 업로드 중 오류 발생");
        }
        return ApiSuccessResponse.response(ResponseCode.Ok, "JSON 파일 업로드 성공!", null);
    }
}