package com.example.knockknock.error.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    BAD_REQUEST(400000, "Bad Request"),
    AUTH_ERROR(401000, "Invalid Token"),
    INVALID_USER(401001, "Deactivated User"),
    NOT_FOUND(404000, "Not Found"),
    SERVER_ERROR(500000, "Unexpected error");


    int code;
    String message;
}
