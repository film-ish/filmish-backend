package com.example.knockknock.controller;

import com.example.knockknock.controller.request.UserRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    @PostMapping
    @Operation(summary = "회원 가입", description = "회원 가입을 시도합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 가입됨")
    })
    public ApiResponse join(@RequestBody UserRequest.JoinRequest request){
        return userService.join(request);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 시도합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 탈퇴함"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
    })
    public void withdraw(@PathVariable Long userId, HttpServletRequest request, HttpServletResponse response){
        userService.withdraw(userId, request, response);
    }

    @GetMapping("")
    @Operation(summary = "이메일 또는 닉네임 중복 확인 조회", description = "이메일 또는 닉네임 중복 확인을 시도합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함"),
    })
    public ApiResponse checkDuplicate(
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "nickname", required = false) String nickname
    ) {
        if (email != null) {
            return userService.checkEmail(email);
        } else if (nickname != null) {
            return userService.checkNickname(nickname);
        }
        return null;
    }

    @GetMapping("/{userId}")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함"),
    })
    public ApiResponse userInfo(@PathVariable Long userId){
        return userService.userInfo(userId);
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀 번호 수정", description = "비밀 번호를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀 번호 수정 완료"),
    })
    public ApiResponse modifyPassword(HttpServletRequest request, Authentication authentication){
        try {
            // Read request body
            byte[] inputStreamBytes = StreamUtils.copyToByteArray(request.getInputStream());
            String requestBodyJsonString = new String(inputStreamBytes, StandardCharsets.UTF_8);

            if (requestBodyJsonString.isEmpty()) {
                log.error("body에 요청 값이 없습니다.");
                return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "Empty request body");
            }

            // requestBody를 JSON으로 파싱하여 newPassword를 추출
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(requestBodyJsonString);

            // 요청에 "newPassword"라는 키가 없으면
            if (jsonNode == null || !jsonNode.has("newPassword")) {
                log.error("요청에 newPassword 값이 없습니다.");
                return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "New password is required");
            }

            String newPassword = jsonNode.get("newPassword").asText();

            // "newPassword"라는 키의 값(value)이 없을 때
            if (newPassword == null || newPassword.trim().isEmpty()) {
                log.error("newPassword의 값이 입력되지 않았습니다.");
                return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "New password must be entered");
            }

            return userService.modifyPassword(authentication, newPassword);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류 발생: " + e);
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "Invalid JSON format");
        } catch (IOException e) {
            log.error("비밀 번호 요청 오류 발생: " + e);
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "Invalid request");
        }
    }
}
