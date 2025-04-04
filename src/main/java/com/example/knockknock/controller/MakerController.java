package com.example.knockknock.controller;


import com.example.knockknock.controller.request.MakerRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.service.MakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/makers")
@RequiredArgsConstructor
@Slf4j
public class MakerController {
    private final MakerService makerService;

    @GetMapping("")
    @Operation(summary = "영화인 목록 조회 및 검색", description = "영화인의 목록을 조회하거나 이름을 검색합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse makerListOrSearch(
            @RequestParam(name = "page", defaultValue = "0") int pageNum,
            @RequestParam(name = "size", defaultValue = "10") int pageSize,
            @RequestParam(name = "name", required = false) String name) {

        if (name != null && !name.isEmpty()) {
            return makerService.searchMaker(name);
        } else {
            return makerService.makerList(pageNum, pageSize);
        }
    }

    @GetMapping("/{makerId}")
    @Operation(summary = "영화인 상세 조회", description = "영화인을 상세 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 조회함")
    })
    public ApiResponse detailMaker(@PathVariable Long makerId){
        return makerService.detailMaker(makerId);
    }

    @PatchMapping("/{makerId}")
    @Operation(summary = "영화인 정보 수정", description = "영화인 정보를 수정합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 수정함")
    })
    public ApiResponse updateMaker(@PathVariable Long makerId, @RequestBody MakerRequest.Update request){
        return makerService.updateMaker(request, makerId);
    }

}
