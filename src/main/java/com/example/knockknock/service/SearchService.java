package com.example.knockknock.service;

import com.example.knockknock.controller.response.*;
import com.example.knockknock.entity.*;
import com.example.knockknock.global.config.jwt.TokenProvider;
import com.example.knockknock.repository.*;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final UserRepository userRepository;
    private final QnaRepository qnaRepository;
    private final IndieMovieRepository indieMovieRepository;
    private final MakerRepository makerRepository;
    private final LikeIndieRepository likeIndieRepository;
    private final TokenProvider tokenProvider;
    private final RestHighLevelClient restHighLevelClient;
    private static final int PAGESIZE = 20;

    public ApiResponse totalSearch(String query, int pageNum, HttpServletRequest request) {
        List<SearchResponse.MovieDetail> findTitleMovies = movieSearch(query, pageNum, request);
        List<SearchResponse.MakerDetail> findDirectors = directorSearch(query, pageNum);
        List<SearchResponse.MakerDetail> findActors = actorSearch(query, pageNum);
        List<SearchResponse.KeyMovies> findGenreMovies = genreSearch(query, pageNum, request);
        List<SearchResponse.KeyMovies> findKeywordMovies = keywordSearch(query, pageNum, request);

        SearchResponse.ListResult searchResult = SearchResponse.ListResult.of(findTitleMovies, findDirectors, findActors, findGenreMovies, findKeywordMovies);
        return ApiSuccessResponse.response(ResponseCode.Ok, "성공적으로 조회하였습니다.", searchResult);
    }


    // 영화 제목 검색
    public List<SearchResponse.MovieDetail> movieSearch(String query, int pageNum, HttpServletRequest request){
        Long userId = null;
        String accessToken = request.getHeader("access");

        if (accessToken != null) {
            String userEmail = tokenProvider.getUserEmail(accessToken);
            User user = userRepository.findByEmail(userEmail).orElse(null);
            userId = user != null ? user.getId() : null;
        }

        List<SearchResponse.MovieDetail> findTitleMovies = new ArrayList<>();
        try {
            SearchRequest searchTitleRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchTitleSourceBuilder = new SearchSourceBuilder();
            searchTitleSourceBuilder.query(QueryBuilders.boolQuery()
                            .should(QueryBuilders.matchBoolPrefixQuery("title", query))
                            .should(QueryBuilders.wildcardQuery("title", "*" + query + "*")))
                            .from(pageNum * PAGESIZE)
                            .size(PAGESIZE);
            searchTitleRequest.source(searchTitleSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchMovieResponse =
                    restHighLevelClient.search(searchTitleRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchMovieResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();

                String movieId = sourceMap.get("movie_id") != null ? sourceMap.get("movie_id").toString() : null;
                if (movieId == null) continue;

                IndieMovie movie = indieMovieRepository.findById(Long.parseLong(movieId)).orElse(null);
                String poster = movie.getPosters().isEmpty() ? null : movie.getPosters().get(0).getThumbnail();

                if (poster == null){
                    // 스틸컷 주소
                    poster = movie.getStillcuts().isEmpty() ? null : movie.getStillcuts().get(0).getStillcut();
                    }

                boolean like = false;
                if (userId != null){
                    Optional<LikeIndie> optLikeIndie = likeIndieRepository.findByIndieMovieIdAndUserId(Long.parseLong(movieId), userId);
                    like = optLikeIndie.isPresent() ? true : false;
                }

                List<String> genres = movie.getGenres().stream()
                        .map(indieGenre -> indieGenre.getGenre().getName())
                        .collect(Collectors.toList());

                findTitleMovies.add(SearchResponse.MovieDetail.of(movie, poster, genres, like));
            }
            log.info("영화 조회 완료");
            return findTitleMovies;
        } catch (IOException e) {
            log.info("영화 검색 중 오류 발생");
            return null;
        }
    }


    // 영화인(배우) 검색
    public List<SearchResponse.MakerDetail> actorSearch(String query, int pageNum) {
        List<SearchResponse.MakerDetail> findActors = new ArrayList<>();
        Set<String> addedActorIds = new HashSet<>();  // 중복 방지용
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
                        String actorId = (String) actor.get("id");
                        // 중복 체크
                        if (addedActorIds.contains(actorId) || !query.equals(name)) continue;

                        Optional<Maker> optMaker = makerRepository.findById(Long.parseLong(actorId));
                        optMaker.ifPresent(maker -> {
                            Long qnaNum = qnaRepository.countByMakerId(maker.getId());
                            List<String> filmography = indieMovieRepository.findByMakerId(maker.getId()).stream()
                                    .map(IndieMovie::getTitle).toList();
                            findActors.add(SearchResponse.MakerDetail.of(maker, qnaNum, filmography));
                            addedActorIds.add(actorId);  // 중복 방지를 위해 추가
                        });
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
    public List<SearchResponse.MakerDetail> directorSearch(String query, int pageNum) {
        List<SearchResponse.MakerDetail> findDirectors = new ArrayList<>();
        Set<String> addedDirectorIds = new HashSet<>();  // 중복 방지용
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
                        String directorId = (String) director.get("id");
                        // 중복 체크
                        if (addedDirectorIds.contains(directorId) || !query.equals(name)) continue;

                        Optional<Maker> optMaker = makerRepository.findById(Long.parseLong(directorId));
                        optMaker.ifPresent(d -> {
                            Long qnaNum = qnaRepository.countByMakerId(d.getId());
                            List<String> filmography = indieMovieRepository.findByMakerId(d.getId()).stream()
                                    .map(IndieMovie::getTitle).toList();
                            findDirectors.add(SearchResponse.MakerDetail.of(d, qnaNum, filmography));
                            addedDirectorIds.add(directorId);  // 중복 방지를 위해 추가
                        });
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
    public List<SearchResponse.KeyMovies> genreSearch(String query, int pageNum, HttpServletRequest request) {
        Long userId = null;
        String accessToken = request.getHeader("access");

        if (accessToken != null) {
            String userEmail = tokenProvider.getUserEmail(accessToken);
            User user = userRepository.findByEmail(userEmail).orElse(null);
            userId = user != null ? user.getId() : null;
        }

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
                String movieId = (String) sourceMap.get("movie_id");
                if (movieId == null) continue;

                IndieMovie movie = indieMovieRepository.findById(Long.parseLong(movieId)).orElse(null);

                String poster = movie.getPosters().isEmpty() ? null : movie.getPosters().get(0).getThumbnail();

                if (poster == null) {
                    // 스틸컷 주소
                    poster = movie.getStillcuts().isEmpty() ? null : movie.getStillcuts().get(0).getStillcut();
                }

                boolean like = false;
                if (userId != null) {
                    Optional<LikeIndie> optLikeIndie = likeIndieRepository.findByIndieMovieIdAndUserId(Long.parseLong(movieId), userId);
                    like = optLikeIndie.isPresent() ? true : false;
                }

                List<String> genres = movie.getGenres().stream()
                        .map(indieGenre -> indieGenre.getGenre().getName())
                        .collect(Collectors.toList());
                movies.add(SearchResponse.MovieDetail.of(movie, poster, genres, like));
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
    public List<SearchResponse.KeyMovies> keywordSearch(String query, int pageNum, HttpServletRequest request){
        Long userId = null;
        String accessToken = request.getHeader("access");

        if (accessToken != null) {
            String userEmail = tokenProvider.getUserEmail(accessToken);
            User user = userRepository.findByEmail(userEmail).orElse(null);
            userId = user != null ? user.getId() : null;
        }

        List<SearchResponse.KeyMovies> findKeywordMovies = new ArrayList<>();
        try {
            SearchRequest searchKeywordRequest = new SearchRequest("movie_ngram");
            SearchSourceBuilder searchKeywordSourceBuilder = new SearchSourceBuilder();
            searchKeywordSourceBuilder.query(QueryBuilders.matchQuery("keywords.name", query))
                    .from(pageNum * PAGESIZE)
                    .size(PAGESIZE);
            searchKeywordRequest.source(searchKeywordSourceBuilder);

            org.elasticsearch.action.search.SearchResponse searchKeywordResponse =
                    restHighLevelClient.search(searchKeywordRequest, RequestOptions.DEFAULT);

            List<SearchResponse.MovieDetail> movies = new ArrayList<>();
            for (SearchHit hit : searchKeywordResponse.getHits().getHits()) {
                Map<String, Object> sourceMap = hit.getSourceAsMap();
                String movieId = (String) sourceMap.get("movie_id");
                if (movieId == null) continue;

                Optional<IndieMovie> optIndieMovie = indieMovieRepository.findById(Long.parseLong(movieId));
                if (optIndieMovie.isPresent()) {
                    IndieMovie movie = optIndieMovie.get();

                    String poster = movie.getPosters().isEmpty() ? null : movie.getPosters().get(0).getPoster();

                    if (poster == null) {
                        // 스틸컷 주소
                        poster = movie.getStillcuts().isEmpty() ? null : movie.getStillcuts().get(0).getStillcut();
                    }

                    boolean like = false;
                    if (userId != null) {
                        Optional<LikeIndie> optLikeIndie = likeIndieRepository.findByIndieMovieIdAndUserId(Long.parseLong(movieId), userId);
                        like = optLikeIndie.isPresent() ? true : false;
                    }

                    List<String> genres = movie.getGenres().stream()
                            .map(indieGenre -> indieGenre.getGenre().getName())
                            .collect(Collectors.toList());
                    movies.add(SearchResponse.MovieDetail.of(movie, poster, genres, like));
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
