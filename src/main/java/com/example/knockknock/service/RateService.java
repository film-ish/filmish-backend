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
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
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
import java.util.Optional;

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

    public ApiResponse createRate(RateRequest.Create request, Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        System.out.println("userId:" + userId);
        User user = userRepository.findById(userId).get();
        System.out.println("indieID:" + request.getIndieId());
        IndieMovie indieMovie = indieMovieRepository.findById(request.getIndieId()).get();

        // 이미 등록된 평점이 있는지 확인
        Optional<Rate> existingRate = rateRepository.findByUserIdAndIndieMovieId(userId, request.getIndieId());

        if (existingRate.isPresent()) {
            // 이미 평점이 존재하면 오류 응답 반환
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미 해당 영화에 평점을 등록하셨습니다.");
        }

        Rate newRate = Rate.builder()
                .value(request.getValue())
                .content(request.getContent())
                .indieMovie(indieMovie)
                .user(user)
                .createdAt(Instant.now())
                .build();

        rateRepository.save(newRate);

        // 평균 평점 계산 및 업데이트
        Double newAverage = rateRepository.findAverageRatingByIndieMovieId(request.getIndieId());
        indieMovie.setAverageRating(newAverage != null ? newAverage.floatValue() : 0.0f);
        indieMovieRepository.save(indieMovie);
        return ApiSuccessResponse.response(ResponseCode.Created, "평점이 성공적으로 등록되었습니다.", null);
    }

    public ApiResponse detailRate(Long rateId){
        Optional<Rate> selectRate = rateRepository.findById(rateId);
        log.info("입력된 rateId = " + rateId);
        if (selectRate.isEmpty()){
            return ApiErrorResponse.of(ErrorCode.NOT_FOUND, "해당 평점을 찾을 수 없습니다.");
        }
        Rate rate = selectRate.get();

        RateResponse.Detail rateResponse = RateResponse.Detail.of(
                rate,
                rate.getUser().getNickname(),
                rate.getUser().getHeadImage()
        );
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 평점을 성공적으로 조회했습니다.", rateResponse);
    }

    public ApiResponse updateRate(RateRequest.Update request, Long rateId){
        Rate rate = rateRepository.findById(rateId).get();

        if(rate.getDeletedAt() != null){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미 삭제된 평점입니다.");
        }
        rate.setValue(request.getValue());
        rate.setContent(request.getContent());

        rateRepository.save(rate);

        // 평균 재계산
        Double newAverage = rateRepository.findAverageRatingByIndieMovieId(rate.getIndieMovie().getId());
        IndieMovie indieMovie = rate.getIndieMovie();
        indieMovie.setAverageRating(newAverage != null ? newAverage.floatValue() : 0.0f);
        indieMovieRepository.save(indieMovie);

        return ApiSuccessResponse.response(ResponseCode.Ok, "평점 수정이 완료되었습니다.", null);
    }

    public ApiResponse deleteRate(Long rateId){
        Rate rate = rateRepository.findById(rateId).get();
        if(rate.isSoftDeleted()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미 삭제된 평점입니다.");
        }
        rate.deleteSoftly(Instant.now());
        rateRepository.save(rate);

        // 평균 재계산
        Double newAverage = rateRepository.findAverageRatingByIndieMovieId(rate.getIndieMovie().getId());
        IndieMovie indieMovie = rate.getIndieMovie();
        indieMovie.setAverageRating(newAverage != null ? newAverage.floatValue() : 0.0f);
        indieMovieRepository.save(indieMovie);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 평점을 성공적으로 삭제했습니다.", null);
    }


}
