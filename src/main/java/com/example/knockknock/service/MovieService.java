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
    private final CommercialMovieRepository commercialMovieRepository;
    private final CommercialGenreRepository commercialGenreRepository;
    private final LikeCommercialRepository likeCommercialRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;


    public ApiResponse likeIndie(MovieRequest.LikeIndie request, CustomUserDetails customUserDetails){
        Long userId = customUserDetails.getUserId();
        Long indieId = request.getIndieId();

        User userEntity = userRepository.findById(userId).get();
        IndieMovie indieMovie = indieMovieRepository.findById(indieId).get();

        // 이미 좋아요 등록한 내역이 있는지 확인
        Optional<LikeIndie> likedIndie = likeIndieRepository.findByIndieMovieIdAndUserId(indieId, userId);
        if (likedIndie.isPresent()){
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이미 등록된 영화입니다.");
        }

        LikeIndie likeIndie = com.example.knockknock.entity.LikeIndie.builder()
                                .user(userEntity)
                                .indieMovie(indieMovie)
                                .build();

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

    public ApiResponse listCommercial(){
        // 랜덤 숫자를 생성, 중복 방지를 위해 Set 사용
        Set<Long> randomIds = new HashSet<>();
        while (randomIds.size() < 20) {
            Long randomNumber = (long) (Math.random() * 226) + 1;
            randomIds.add(randomNumber);
        }

        // DB 호출 횟수 최소화를 위해 랜덤 ID 리스트를 한 번에 조회
        List<CommercialMovie> movies = commercialMovieRepository.findAllById(randomIds);
        Map<Long, List<CommercialGenre>> genresMap = commercialGenreRepository.findByCommercialIdIn(randomIds).stream()
                                                        .collect(Collectors.groupingBy(CommercialGenre::getCommercialId));

        List<CommercialResponse.Detail> movieList = movies.stream()
                .map(movie -> {
                    List<String> categories = genresMap.getOrDefault(movie.getId(), Collections.emptyList())
                            .stream()
                            .map(genre -> genre.getGenre().getName())
                            .toList();
                    return CommercialResponse.Detail.of(movie, categories);
                })
                .toList();

        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", movieList);
    }

    public ApiResponse likeCommercial(MovieRequest.LikeCommercial request, CustomUserDetails customUserDetails){
        List<Long> commercialId = request.getCommercialId();

        commercialId.forEach(id -> {
            User userEntity = userRepository.findById(customUserDetails.getUserId()).get();
            CommercialMovie movie = commercialMovieRepository.findById(id).get();
            likeCommercialRepository.save(LikeCommercial.builder()
                    .user(userEntity)
                    .commercialMovie(movie)
                    .build());
        });

        log.info("보고싶어요 등록이 완료되었습니다.");
        return ApiSuccessResponse.response(ResponseCode.Created, "보고싶어요 등록이 완료되었습니다.", null);
    }


}
