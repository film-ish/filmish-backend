package com.example.knockknock.service;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.MakerResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Maker;
import com.example.knockknock.entity.MakerMovie;
import com.example.knockknock.entity.UserMaker;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.MakerMovieRepository;
import com.example.knockknock.repository.MakerRepository;
import com.example.knockknock.repository.QnaRepository;
import com.example.knockknock.repository.UserMakerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MakerService {
    private final MakerMovieRepository makerMovieRepository;
    private final UserMakerRepository userMakerRepository;
    private final QnaRepository qnaRepository;
    private final MakerRepository makerRepository;

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

        List<MakerResponse.Filmography> filmography = makerMovies.stream()
                .map(makerMovie -> MakerResponse.Filmography.of(makerMovie.getIndieMovie()))
                .toList();

        MakerResponse.Detail detail = MakerResponse.Detail.of(
                maker,
                userId,
                qnaCnt,
                filmography
        );
        return ApiSuccessResponse.response(ResponseCode.Ok, "상세 페이지가 조회되었습니다.", detail);
    }
}