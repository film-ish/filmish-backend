package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.MovieRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.MovieResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.LikeIndie;
import com.example.knockknock.entity.User;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.IndieMovieRepository;
import com.example.knockknock.repository.LikeIndieRepository;
import com.example.knockknock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final IndieMovieRepository indieMovieRepository;
    private final LikeIndieRepository likeIndieRepository;
    private final UserRepository userRepository;
    public ApiResponse likeIndie(MovieRequest.LikeIndieRequest request, Authentication authentication){
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUserId();
        Long indieId = request.getIndieId();

        User userEntity = userRepository.findById(userId).get();
        IndieMovie indieMovie = indieMovieRepository.findById(indieId).get();

        log.info("좋아요 등록 로직 실행 ");

        // 이미 좋아요 등록한 내역이 있는지 확인
        Optional<LikeIndie> likedIndie = likeIndieRepository.findByIndieMovieIdAndUserId(indieId, userId);
        log.info(likedIndie.toString());
        log.info("userId = " + userId + ", indieId = " + indieId);
        log.info("likeIndie.isEmpty(): " + likedIndie.isEmpty());
        if (likedIndie.isPresent()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "already liked this movie");
        }



        LikeIndie likeIndie = LikeIndie.builder()
                                .user(userEntity)
                                .indieMovie(indieMovie)
                                .build();

        log.info("likeIndie 객체 생성");

        likeIndieRepository.save(likeIndie);

        log.info("보고싶어요 등록이 완료되었습니다.");

        return ApiSuccessResponse.response(ResponseCode.Created, "보고싶어요 등록이 완료되었습니다.", null);
    }

    public ApiResponse unlikeIndie(Long likeId){
        Optional<LikeIndie> likedIndie = likeIndieRepository.findById((likeId));
        if (likedIndie.isEmpty()) {
            return ApiErrorResponse.of(ErrorCode.NOT_FOUND, "등록된 보고싶어요 내역이 없습니다.");
        }
        likeIndieRepository.delete(likedIndie.get());

        log.info("보고싶어요 삭제가 완료되었습니다.");
        return ApiSuccessResponse.response(ResponseCode.Ok, "보고싶어요 삭제가 완료되었습니다.", null);
    }

    public ApiResponse movieInfo(Long movieId){
        Optional<IndieMovie> indieMovie = indieMovieRepository.findById(movieId);
        if (indieMovie.isEmpty()) {
            return ApiErrorResponse.of(ErrorCode.NOT_FOUND, "조회한 영화가 존재하지 않습니다.");
        }
        //TODO
        // 왜 타입이 안 나오지?
        log.info("indieMovie.get().getType() = " + indieMovie.get().getType());
        log.info("영화 조회가 완료되었습니다.");
        MovieResponse.Info info = MovieResponse.Info.of(indieMovie.get());
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 조회가 완료되었습니다.", info);
    }
}
