package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.QnaRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.Maker;
import com.example.knockknock.entity.Qna;
import com.example.knockknock.entity.User;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.MakerRepository;
import com.example.knockknock.repository.QnaRepository;
import com.example.knockknock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class QnaService {
    private final UserRepository userRepository;
    private final MakerRepository makerRepository;
    private final QnaRepository qnaRepository;

    public ApiResponse writeQna(QnaRequest.WriteQna request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        User writer = userRepository.findById(userId).get();
        Maker maker = makerRepository.findById(request.getMakerId()).get();

        Qna newQna = Qna.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(Instant.now())
                .maker(maker)
                .user(writer)
                .build();

        qnaRepository.save(newQna);
        return ApiSuccessResponse.response(ResponseCode.Created, "QnA가 성공적으로 등록되었습니다.", null);
    }

    public ApiResponse updateQna(Long qnaId, QnaRequest.Update request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Qna qna = qnaRepository.findById(qnaId).get();

        if (qna.getUser().getId() != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "수정 권한이 없습니다.");
        }

        qna.setTitle(request.getTitle());
        qna.setContent(request.getContent());
        qna.setUpdatedAt(Instant.now());

        qnaRepository.save(qna);

        return ApiSuccessResponse.response(ResponseCode.Ok, "수정이 완료되었습니다.", null);
    }
}
