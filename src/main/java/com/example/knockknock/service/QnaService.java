package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.QnaRequest;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.Maker;
import com.example.knockknock.entity.Qna;
import com.example.knockknock.entity.QnaComment;
import com.example.knockknock.entity.User;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.MakerRepository;
import com.example.knockknock.repository.QnaCommentRepository;
import com.example.knockknock.repository.QnaRepository;
import com.example.knockknock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QnaService {
    private final UserRepository userRepository;
    private final MakerRepository makerRepository;
    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;

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

        return ApiSuccessResponse.response(ResponseCode.Ok, "게시물 수정이 완료되었습니다.", null);
    }

    public ApiResponse deleteQna(Long qnaId, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Qna qna = qnaRepository.findById(qnaId).get();

        if(qna.getUser().getId() != userDetails.getUserId()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "권한이 없습니다.");
        }

        qna.deleteSoftly(Instant.now());
        qnaRepository.save(qna);

        return ApiSuccessResponse.response(ResponseCode.Ok, "게시물이 삭제되었습니다.", null);
    }

    public ApiResponse listQna(Long makerId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<QnaResponse.Detail> qnaPage = qnaRepository.findByMakerId(makerId, pageable)
                .map(qna -> {
                    User writer = qna.getUser();
                    List<QnaComment> qnaCommentList = null;
                    List<QnaCommentResponse.Detail> qnaComments = null;
                    Optional<List<QnaComment>> commentList = qnaCommentRepository.findByQnaId(qna.getId());
                    if(!commentList.isEmpty()){
                        qnaCommentList = commentList.get();
                    }
                    qnaComments = qnaCommentList.stream()
                            .map(qnaComment -> {
                                User commentWriter = qnaComment.getUser();
                                Optional<List<QnaComment>> subComments = qnaCommentRepository
                                        .findByParentCommentId(qnaComment.getId());

                                List<QnaComment> subList = null;
                                List<QnaCommentResponse.Detail> subCommentList = null;
                                if(!subComments.isEmpty()) {
                                    subList = subComments.get();
                                    subCommentList = subList.stream()
                                            .map(subComment -> {
                                                User subCommentWriter = subComment.getUser();
                                                return QnaCommentResponse.Detail.of(
                                                        subComment,
                                                        subCommentWriter,
                                                        null
                                                );
                                            }).toList();
                                }
                                return QnaCommentResponse.Detail.of(qnaComment, commentWriter, subCommentList);
                            }).toList();
                    return QnaResponse.Detail.of(qna, writer,qnaComments);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "목록이 성공적으로 조회되었습니다.", qnaPage);
    }
}
