package com.example.knockknock.controller.response;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ApiSuccessResponse<E> extends ApiResponse{
    private ResponseCode code;
    private E data;

    public ApiSuccessResponse(ResponseCode code, String message, E data) {
        super(message);
        this.code = code;
        this.data = data;
    }

    public static <E> ApiSuccessResponse<E> response(ResponseCode code, E data) {
        return ApiSuccessResponse.<E>builder()
                .code(code)
                .data(data)
                .build();
    }

    public static <E> ApiSuccessResponse<E> response(ResponseCode code, String message, E data){
        log.info("입력된 message = " + message);
        return ApiSuccessResponse.<E>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
