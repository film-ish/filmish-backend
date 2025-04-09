package com.example.knockknock.service;

import com.example.knockknock.controller.request.MakerRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.MakerResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.*;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MakerService {
    private final MakerMovieRepository makerMovieRepository;
    private final UserMakerRepository userMakerRepository;
    private final QnaRepository qnaRepository;
    private final MakerRepository makerRepository;
    private final PosterRepository posterRepository;
    private final StillcutRepository stillcutRepository;

    public ApiResponse makerList(int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "maker.name"));
        Page<MakerResponse.ListItem> makerPage = makerMovieRepository.findAll(pageable)
                .map(makerMovie -> {
                    Maker maker = makerMovie.getMaker();

                    UserMaker userMaker = userMakerRepository.findUserMakerByMakerId(maker.getId());
                    String email = (userMaker != null && userMaker.getUser() != null)
                            ? userMaker.getUser().getEmail()
                            : null;
                    String role = makerMovie.getType().toString();
                    Long postNum = qnaRepository.countByMakerId(maker.getId());
                    Long movieNum = makerMovieRepository.countByMakerId(maker.getId());
                    Optional<IndieMovie> indieMovie = makerMovieRepository.findRandomMovieByMakerId(maker.getId());
                    String movieTitle = indieMovie.map(IndieMovie::getTitle).orElse("No Movie Found");

                    return MakerResponse.ListItem.of(
                            maker,
                            email,
                            role,
                            postNum,
                            movieNum,
                            movieTitle
                    );
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화인 목록이 성공적으로 조회되었습니다.", makerPage);
    }

    public ApiResponse detailMaker(Long makerId) {
        Optional<Maker> selectMaker = makerRepository.findById(makerId);
        log.info("입력된 makerId = " + makerId);

        if (selectMaker.isEmpty()) {
            return ApiErrorResponse.of(ErrorCode.NOT_FOUND, "해당 영화인을 찾을 수 없습니다.");
        }

        Maker maker = selectMaker.get();

        UserMaker userMaker = userMakerRepository.findUserMakerByMakerId(maker.getId());
        Long userId = (userMaker != null && userMaker.getUser() != null)
                ? userMaker.getUser().getId()
                : null;

        Long qnaCnt = qnaRepository.countByMakerId(maker.getId());

        List<MakerMovie> makerMovies = makerMovieRepository.findAllByMakerId(maker.getId());

        // 영화 ID 목록 추출
        List<Long> movieIds = makerMovies.stream()
                .map(makerMovie -> makerMovie.getIndieMovie().getId())
                .toList();

        // 각 영화의 첫 번째 포스터 조회
        Map<Long, String> moviePosterMap = new HashMap<>();
        if (!movieIds.isEmpty()) {
            List<Object[]> posters = posterRepository.findFirstPosterByMovieIds(movieIds);
            for (Object[] poster : posters) {
                Long movieId = (Long) poster[0];
                String posterUrl = (String) poster[1];
                moviePosterMap.put(movieId, posterUrl);
            }
        }

        Map<Long, String> movieStillcutMap = new HashMap<>();
        if (!movieIds.isEmpty()) {
            // 스틸컷을 위한 유사한 쿼리 사용 (posterRepository에 유사한 메소드 추가 필요)
            List<Object[]> stillcuts = stillcutRepository.findFirstStillcutByMovieIds(movieIds);
            for (Object[] stillcut : stillcuts) {
                Long movieId = (Long) stillcut[0];
                String stillcutUrl = (String) stillcut[1];
                movieStillcutMap.put(movieId, stillcutUrl);
            }
        }

        List<MakerResponse.Filmography> filmography = makerMovies.stream()
                .map(makerMovie -> {
                    IndieMovie movie = makerMovie.getIndieMovie();
                    Long movieId = movie.getId();
                    String posterUrl = moviePosterMap.get(movieId);
                    String stillcutUrl = movieStillcutMap.get(movieId);

                    String imageUrl = posterUrl != null ? posterUrl : stillcutUrl;

                    // 포스터 객체 생성 (null 안전성을 위해)
                    Poster poster = null;
                    if (imageUrl != null) {
                        poster = new Poster();
                        poster.setPoster(imageUrl);
                    }

                    return MakerResponse.Filmography.of(movie, poster);
                })
                .collect(Collectors.toList());

        MakerResponse.Detail detail = MakerResponse.Detail.of(
                maker,
                userId,
                qnaCnt,
                filmography
        );
        return ApiSuccessResponse.response(ResponseCode.Ok, "상세 페이지가 조회되었습니다.", detail);
    }

    @Transactional
    public ApiResponse updateMaker(@RequestBody MakerRequest.Update request, Long makerId) {
        Optional<Maker> optionalMaker = makerRepository.findById(makerId);

        if (optionalMaker.isEmpty()) {
            return ApiErrorResponse.of(ErrorCode.NOT_FOUND, "해당 영화인을 찾을 수 없습니다.");
        }

        Maker maker = optionalMaker.get();

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiErrorResponse.of(ErrorCode.BAD_REQUEST, "이름은 필수 항목입니다.");
        }

        maker.setName(request.getName());
        maker.setThumbnailImage(request.getImage());

        makerRepository.save(maker);

        return ApiSuccessResponse.response(ResponseCode.Ok, "영화인 수정이 완료되었습니다.", null);
    }

    public ApiResponse searchMaker(String name) {
        List<MakerMovie> directorMovies = makerMovieRepository.findByMakerNameContainingAndType(name, Type.DIRECTOR);
        List<MakerMovie> actorMovies = makerMovieRepository.findByMakerNameContainingAndType(name, Type.ACTOR);
        List<MakerMovie> actorAndDirectorMovies = makerMovieRepository.findByMakerNameContainingAndType(name, Type.ACTORANDDIRECTOR);

        // 2. 모든 관련 MakerMovie를 하나로 합침
        List<MakerMovie> allRelevantMakerMovies = new ArrayList<>();
        allRelevantMakerMovies.addAll(directorMovies);
        allRelevantMakerMovies.addAll(actorMovies);
        allRelevantMakerMovies.addAll(actorAndDirectorMovies);

        // 3. 관련된 모든 고유 Maker ID 추출
        Set<Long> allMakerIds = allRelevantMakerMovies.stream()
                .map(mm -> mm.getMaker().getId())
                .collect(Collectors.toSet());

        // 4. 각 Maker ID별로 *새로운* MakerSearchResult 정보 생성
        List<MakerResponse.SearchResult> searchResults = allMakerIds.stream()
                .map(makerId -> {
                    Maker maker = makerRepository.findById(makerId).orElse(null);
                    if (maker == null) return null;

                    UserMaker userMaker = userMakerRepository.findUserMakerByMakerId(makerId);
                    Long userId = (userMaker != null && userMaker.getUser() != null) ? userMaker.getUser().getId() : null;
                    Long qnaCount = qnaRepository.countByMakerId(makerId);
                    List<MakerMovie> moviesForThisMaker = allRelevantMakerMovies.stream()
                            .filter(mm -> mm.getMaker().getId().equals(makerId))
                            .toList();

                    // 고유 영화 목록 및 개수 계산 (기존과 동일)
                    Set<Long> uniqueMovieIds = new HashSet<>();
                    List<MakerResponse.SimpleFilmography> simpleFilmographies = moviesForThisMaker.stream()
                            .map(MakerMovie::getIndieMovie)
                            .filter(Objects::nonNull) // 혹시 모를 null IndieMovie 방지
                            .filter(movie -> uniqueMovieIds.add(movie.getId())) // 중복 제거하며 Set에 추가
                            .map(MakerResponse.SimpleFilmography::from) // SimpleFilmography DTO 사용
                            .toList();

                    Long movieCount = (long) uniqueMovieIds.size();

                    return MakerResponse.SearchResult.of(maker, userId, qnaCount, movieCount, simpleFilmographies);
                })
                .filter(Objects::nonNull)
                .toList();

// 5. 최종 응답 생성 (기존과 동일)
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("makers", searchResults); // 결과를 "makers" 키에 담음

        // 실제 사용하는 ResponseCode와 메시지로 변경하세요
        return ApiSuccessResponse.response(ResponseCode.Ok, "검색이 완료되었습니다.", responseData);
    }


}