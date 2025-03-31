package com.example.knockknock.service;

import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.Genre;
import com.example.knockknock.entity.IndieGenre;
import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Poster;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final LikeIndieRepository likeIndieRepository;
    private final IndieMovieRepository indieMovieRepository;
    private final PosterRepository posterRepository;
    private final IndieGenreRepository indieGenreRepository;
    private final GenreRepository genreRepository;
    private final RateRepository rateRepository;

    public ApiResponse listLikeIndie(Long userId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<IndieResponse.LikeDetail> likePage = likeIndieRepository.findByUserId(userId, pageable)
                .map(likeIndie -> {
                    IndieMovie movie = likeIndie.getIndieMovie();
                    List<Poster> posters = posterRepository.findByIndieId(movie.getId());
                    List<IndieGenre> indieGenres = indieGenreRepository.findByIndieId(movie.getId());

                    List<String> categories = indieGenres
                            .stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .toList();

                    String posterUrl = posters.isEmpty() ? null : posters.get(0).getPoster();
                    return IndieResponse.LikeDetail.of(movie, posterUrl, categories);
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", likePage);
    }

    public ApiResponse listRating(Long userId, int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<MypageResponse.RateDetail> ratePage = rateRepository.findByUserId(userId, pageable)
                .map(rate -> {
                    IndieMovie movie = rate.getIndieMovie();
                    Poster poster = posterRepository.findByIndieId(movie.getId()).get(0);

                    return MypageResponse.RateDetail.of(rate, movie, poster.getPoster());
                });
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회되었습니다.", ratePage);
    }
}
