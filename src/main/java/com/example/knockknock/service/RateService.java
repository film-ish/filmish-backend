package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.RateRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.RateResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Rate;
import com.example.knockknock.entity.User;
import com.example.knockknock.repository.IndieMovieRepository;
import com.example.knockknock.repository.RateRepository;
import com.example.knockknock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateService {
    private final UserRepository userRepository;
    private final RateRepository rateRepository;
    private final IndieMovieRepository indieMovieRepository;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public ApiResponse rateList(Long indieId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RateResponse.Detail> ratePage = rateRepository.findByIndieId(indieId, pageable)
                .map(rate -> {
                    User writer = rate.getUser();

                    return RateResponse.Detail.of(
                            rate,
                            writer.getNickname(),
                            writer.getHeadImage()
                    );

                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "평점 목록이 성공적으로 조회되었습니다.", ratePage);
    }

    public ApiResponse writeRate(RateRequest.WriteRate request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId).get();
        IndieMovie indieMovie = indieMovieRepository.findById(request.getIndieId()).get();

        Rate newRate = Rate.builder()
                .value(request.getValue())
                .content(request.getContent())
                .indieMovie(indieMovie)
                .user(user)
                .createdAt(Instant.now())
                .build();
        rateRepository.save(newRate);
        return ApiSuccessResponse.response(ResponseCode.Created, "평점이 성공적으로 등록되었습니다.", null);
    }

}
