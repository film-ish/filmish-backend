package com.example.knockknock.service;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.MakerResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Maker;
import com.example.knockknock.entity.UserMaker;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MakerService {
    private final MakerMovieRepository makerMovieRepository;
    private final UserMakerRepository userMakerRepository;
    private final QnaRepository qnaRepository;

    public ApiResponse makerList(Long makerId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "name"));
        Page<MakerResponse.Detail> makerPage = makerMovieRepository.findByMakerId(makerId, pageable)
                .map(makerMovie -> {
                    Maker maker = makerMovie.getMaker();

                    UserMaker userMaker = userMakerRepository.findUserMakerByMakerId(maker.getId());
                    String email = userMaker.getUser().getEmail();
                    String role = makerMovie.getType().toString();
                    Long postNum = qnaRepository.countByMakerId(maker.getId());
                    Long movieNum = makerMovieRepository.countByMakerId(maker.getId());
                    Optional<IndieMovie> indieMovie = makerMovieRepository.findRandomMovieByMakerId(maker.getId());

                    return MakerResponse.Detail.of(
                            maker,
                            email,
                            role,
                            postNum,
                            movieNum,
                            indieMovie.get().getTitle()
                    );
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "영화인 목록이 성공적으로 조회되었습니다.", makerPage);
    }

}
