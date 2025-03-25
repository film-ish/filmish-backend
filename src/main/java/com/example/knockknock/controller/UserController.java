package com.example.knockknock.controller;

import com.example.knockknock.controller.request.UserRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
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
}
