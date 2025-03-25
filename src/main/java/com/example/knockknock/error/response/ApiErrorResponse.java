package com.example.knockknock.error.response;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.error.code.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiErrorResponse extends ApiResponse {
    private ErrorCode code;
    public ApiErrorResponse(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String message){
        return ApiErrorResponse.builder()
                .code(errorCode)
                .message(message)
                .build();
    }
}
