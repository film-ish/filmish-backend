package com.example.knockknock.controller.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class ApiResponse {
    private String message;

    public ApiResponse( String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
