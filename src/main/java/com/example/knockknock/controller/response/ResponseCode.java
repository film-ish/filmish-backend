package com.example.knockknock.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    Ok(200000),
    Created(201000);

    int code;
}
