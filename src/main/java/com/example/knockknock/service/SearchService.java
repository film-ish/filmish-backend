package com.example.knockknock.service;

import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final PosterRepository posterRepository;
    private final IndieGenreRepository indieGenreRepository;
    private final QnaRepository qnaRepository;
    private final IndieMovieRepository indieMovieRepository;
    private final MakerRepository makerRepository;
    private final LikeIndieRepository likeIndieRepository;
    private final RestHighLevelClient restHighLevelClient;
    private static final int PAGESIZE = 20;

    public ApiResponse totalSearch(String query, int pageNum, CustomUserDetails userDetails) {
        List<SearchResponse.MovieDetail> findTitleMovies = movieSearch(query, pageNum, userDetails);
        List<SearchResponse.MakerDetail> findDirectors = directorSearch(query, pageNum, userDetails);
        List<SearchResponse.MakerDetail> findActors = actorSearch(query, pageNum, userDetails);
        List<SearchResponse.KeyMovies> findGenreMovies = genreSearch(query, pageNum, userDetails);
        List<SearchResponse.KeyMovies> findKeywordMovies = keywordSearch(query, pageNum, userDetails);

        SearchResponse.ListResult searchResult = SearchResponse.ListResult.of(findTitleMovies, findDirectors, findActors, findGenreMovies, findKeywordMovies);
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회하였습니다.", searchResult);
    }


    // 영화 제목 검색
    public List<SearchResponse.MovieDetail> movieSearch(String query, int pageNum, CustomUserDetails userDetails){
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        List<SearchResponse.MovieDetail> findTitleMovies = new ArrayList<>();
        try {
            SearchRequest searchTitleRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchTitleSourceBuilder = new SearchSourceBuilder();
            searchTitleSourceBuilder.query(QueryBuilders.matchQuery("title", query))
                    .from(pageNum * PAGESIZE)
                    .size(PAGESIZE);
            searchTitleRequest.source(searchTitleSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchMovieResponse =
                    restHighLevelClient.search(searchTitleRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchMovieResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();

                String movieId = sourceMap.get("movie_id") != null ? sourceMap.get("movie_id").toString() : null;
                if (movieId == null) continue;

                String poster = posterRepository.findByIndieId(Long.parseLong(movieId))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0).getPoster())
                        .orElse(null);

                boolean like = false;

                if (userId != null){
                    Optional<LikeIndie> optLikeIndie = likeIndieRepository.findByIndieMovieIdAndUserId(Long.parseLong(movieId), userId);
                    like = optLikeIndie.isPresent() ? true : false;
                }
                findTitleMovies.add(SearchResponse.MovieDetail.of(sourceMap, poster, like));
            }
            log.info("영화 조회 완료");
            return findTitleMovies;
        } catch (IOException e) {
            log.info("영화 검색 중 오류 발생");
            return null;
        }
    }


    // 영화인(배우) 검색
    public List<SearchResponse.MakerDetail> actorSearch(String query, int pageNum, CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        List<SearchResponse.MakerDetail> findActors = new ArrayList<>();
        try {
            SearchRequest searchActorRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchActorSourceBuilder = new SearchSourceBuilder();
            searchActorSourceBuilder.query(QueryBuilders.matchQuery("actors.name", query))
                    .from(pageNum * PAGESIZE)
                    .size(PAGESIZE);;
            searchActorRequest.source(searchActorSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchActorResponse =
                    restHighLevelClient.search(searchActorRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchActorResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();
                List<Map<String, Object>> actorList = (List<Map<String, Object>>) sourceMap.get("actors");
                if (actorList != null) {
                    for (Map<String, Object> actor : actorList) {
                        String name = (String) actor.get("name");
                        if (query.equals(name)) {
                            String actorId = (String) actor.get("id");
                            Optional<Maker> optMaker = makerRepository.findById(Long.parseLong(actorId));
                            optMaker.ifPresent(maker -> {
                                Long qnaNum = qnaRepository.countByMakerId(maker.getId());
                                List<String> filmography = indieMovieRepository.findByMakerId(maker.getId()).stream()
                                        .map(IndieMovie::getTitle).toList();
                                findActors.add(SearchResponse.MakerDetail.of(maker, qnaNum, filmography));
                            });
                        }
                    }
                }
            }
            log.info("배우 조회 완료");
            return findActors;
        } catch (IOException e) {
            log.info("배우 검색 중 오류 발생");
            return null;
        }
    }


    // (영화인) 감독 검색
    public List<SearchResponse.MakerDetail> directorSearch(String query, int pageNum, CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        List<SearchResponse.MakerDetail> findDirectors = new ArrayList<>();
        try {
            SearchRequest searchDirectorRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchDirectorSourceBuilder = new SearchSourceBuilder();
            searchDirectorSourceBuilder.query(QueryBuilders.matchQuery("directors.name", query))
                    .from(pageNum * PAGESIZE)
                    .size(PAGESIZE);;
            searchDirectorRequest.source(searchDirectorSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchDirectorResponse =
                    restHighLevelClient.search(searchDirectorRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchDirectorResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();
                List<Map<String, Object>> directorList = (List<Map<String, Object>>) sourceMap.get("directors");
                if (directorList != null) {
                    for (Map<String, Object> director : directorList) {
                        String name = (String) director.get("name");
                        if (query.equals(name)) {
                            String directorId = (String) director.get("id");
                            Optional<Maker> optMaker = makerRepository.findById(Long.parseLong(directorId));
                            optMaker.ifPresent(d -> {
                                Long qnaNum = qnaRepository.countByMakerId(d.getId());
                                List<String> filmography = indieMovieRepository.findByMakerId(d.getId()).stream()
                                        .map(IndieMovie::getTitle).toList();
                                findDirectors.add(SearchResponse.MakerDetail.of(d, qnaNum, filmography));
                            });
                        }
                    }
                }
            }
            log.info("감독 조회 완료");
            return findDirectors;
        } catch (IOException e) {
            log.info("감독 검색 중 오류 발생");
            return null;
        }
    }

    
    // 장르 검색
    public List<SearchResponse.KeyMovies> genreSearch(String query, int pageNum, CustomUserDetails userDetails) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        List<SearchResponse.KeyMovies> findGenreMovies = new ArrayList<>();
        try {
            SearchRequest searchGenreRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchGenreSourceBuilder = new SearchSourceBuilder();
            searchGenreSourceBuilder.query(QueryBuilders.matchQuery("genres.name", query))
                    .from(pageNum * PAGESIZE)
                    .size(PAGESIZE);
            searchGenreRequest.source(searchGenreSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchGenreResponse =
                    restHighLevelClient.search(searchGenreRequest, RequestOptions.DEFAULT);

            List<SearchResponse.MovieDetail> movies = new ArrayList<>();
            for (SearchHit hit : searchGenreResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();
                String indieId = (String) sourceMap.get("movie_id");
                if (indieId == null) continue;

                Optional<IndieMovie> optIndieMovie = indieMovieRepository.findById(Long.parseLong(indieId));
                if (optIndieMovie.isPresent()) {
                    IndieMovie indieMovie = optIndieMovie.get();
                    String poster = posterRepository.findByIndieId(indieMovie.getId())
                            .filter(posterList -> !posterList.isEmpty())
                            .map(posters -> posters.get(0).getPoster())
                            .orElse(null);

                    boolean like = false;

                    if (userId != null){
                        Optional<LikeIndie> optLikeIndie = likeIndieRepository.findByIndieMovieIdAndUserId(indieMovie.getId(), userId);
                        like = optLikeIndie.isPresent() ? true : false;
                    }
                    Optional<List<IndieGenre>> optGenres = indieGenreRepository.findByIndieId(indieMovie.getId());
                    List<String> genres = optGenres.isPresent() && !optGenres.get().isEmpty() ?
                            optGenres.get().stream().map(indieGenre -> indieGenre.getGenre().getName()).toList() : null;
                    movies.add(SearchResponse.MovieDetail.of(indieMovie, poster, genres, like));
                }
            }
            if (!movies.isEmpty()) {
                findGenreMovies.add(SearchResponse.KeyMovies.of(query, movies));
            }
            log.info("장르 영화 조회 완료");
            return findGenreMovies;
        } catch (IOException e) {
            log.info("장르 검색 중 오류 발생 : {}", e.getMessage());
            return null;
        }
    }


    // 키워드 검색
    public List<SearchResponse.KeyMovies> keywordSearch(String query, int pageNum, CustomUserDetails userDetails){
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        List<SearchResponse.KeyMovies> findKeywordMovies = new ArrayList<>();
        try {
            SearchRequest searchKeywordRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchKeywordSourceBuilder = new SearchSourceBuilder();
            searchKeywordSourceBuilder.query(QueryBuilders.matchQuery("keywords.name", query))
                    .from(pageNum * PAGESIZE)
                    .size(PAGESIZE);;
            searchKeywordRequest.source(searchKeywordSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchKeywordResponse =
                    restHighLevelClient.search(searchKeywordRequest, RequestOptions.DEFAULT);

            List<SearchResponse.MovieDetail> movies = new ArrayList<>();
            for (SearchHit hit : searchKeywordResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();
                String indieId = (String) sourceMap.get("movie_id");
                if (indieId == null) continue;

                Optional<IndieMovie> optIndieMovie = indieMovieRepository.findById(Long.parseLong(indieId));
                if (optIndieMovie.isPresent()) {
                    IndieMovie indieMovie = optIndieMovie.get();
                    String poster = posterRepository.findByIndieId(indieMovie.getId())
                            .filter(posterList -> !posterList.isEmpty())
                            .map(posters -> posters.get(0).getPoster())
                            .orElse(null);

                    boolean like = false;

                    if (userId != null){
                        Optional<LikeIndie> optLikeIndie = likeIndieRepository.findByIndieMovieIdAndUserId(indieMovie.getId(), userId);
                        like = optLikeIndie.isPresent() ? true : false;
                    }
                    Optional<List<IndieGenre>> optGenres = indieGenreRepository.findByIndieId(indieMovie.getId());
                    List<String> genres = optGenres.isPresent() && !optGenres.get().isEmpty() ?
                            optGenres.get().stream().map(indieGenre -> indieGenre.getGenre().getName()).toList() : null;
                    movies.add(SearchResponse.MovieDetail.of(indieMovie, poster, genres, like));
                }
            }
            if (!movies.isEmpty()) {
                findKeywordMovies.add(SearchResponse.KeyMovies.of(query, movies));
            }
            log.info("키워드 영화 조회 완료");
            return findKeywordMovies;
        } catch (IOException e) {
            log.info("키워드 조회 중 오류 발생");
            return null;
        }
    }
}
