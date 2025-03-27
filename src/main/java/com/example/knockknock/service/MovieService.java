package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.request.MovieRequest;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {
    private final IndieMovieRepository indieMovieRepository;
    private final LikeIndieRepository likeIndieRepository;
    private final UserRepository userRepository;
    private final StillcutRepository stillcutRepository;
    private final MakerMovieRepository makerMovieRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;


    public ApiResponse likeIndie(MovieRequest.LikeIndie request, Authentication authentication){
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



        LikeIndie likeIndie = com.example.knockknock.entity.LikeIndie.builder()
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

        List<Map<Long, String>> stillcuts = null;
        Optional<List<Stillcut>> stillcutList = stillcutRepository.findByIndieId(movieId);
        if (!stillcutList.isEmpty()){
            stillcuts = stillcutList.get()
                            .stream()
                            .map(stillcut -> {
                                Map<Long, String> map = new HashMap<>();
                                map.put(stillcut.getId(), stillcut.getStillcut());
                                return map;
                            })
                            .collect(Collectors.toList());
        }

        List<Maker> staff = null;
        List<Map<Maker, Type>> makers = null;
        List<MakerResponse.Role> roles = null;

        Optional<List<MakerMovie>> staffList = makerMovieRepository.findByIndieId(movieId);
        if(!staffList.isEmpty()){
            staff = staffList.get()
                    .stream()
                    .map(MakerMovie::getMaker)
                    .toList();

            makers = staffList.get()
                    .stream()
                    .map(makerMovie -> {
                        Map<Maker, Type> map = new HashMap<>();
                        map.put(makerMovie.getMaker(), makerMovie.getType());
                        return map;
                    })
                    .toList();

            roles = makers.stream()
                    .flatMap(map -> map.entrySet().stream())
                    .map(entry -> MakerResponse.Role.of(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        log.info("영화 조회가 완료되었습니다.");
        MovieResponse.Detail detail = MovieResponse.Detail.of(indieMovie.get(), stillcuts, roles);
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화 조회가 완료되었습니다.", detail);
    }




}
