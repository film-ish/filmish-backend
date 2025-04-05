package com.example.knockknock.controller;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.UserRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse join(@RequestBody UserRequest.Join request){
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

    // 회원 정보 조회
    @GetMapping("/{userId}")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함"),
    })
    public ApiResponse userInfo(@PathVariable Long userId){
        return userService.userInfo(userId);
    }

    // 회원 정보 수정
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "회원 정보 수정", description = "회원 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정함"),
    })
    public ApiResponse updateUser(@PathVariable Long userId,
                                  @ModelAttribute UserRequest.Modify Modify){

        return userService.updateUser(userId, Modify);
    }


    @PatchMapping("/password")
    @Operation(summary = "비밀번호 수정", description = "비밀번호를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 수정 완료"),
    })
    public ApiResponse modifyPassword(@RequestBody UserRequest.ModifyPassword ModifyPassword,
                                      @AuthenticationPrincipal CustomUserDetails userDetails){

        String newPassword = ModifyPassword.getNewPassword();
        return userService.modifyPassword(userDetails, newPassword);
    }
}
