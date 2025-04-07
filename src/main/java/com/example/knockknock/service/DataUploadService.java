package com.example.knockknock.service;

import com.example.knockknock.controller.request.MovieDocumentRequest;
import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataUploadService {
    private final RestHighLevelClient esClient;
    private final ObjectMapper objectMapper;

    public ApiResponse uploadJsonFileToElastic() throws IOException {
        try {
            // 1. 파일 로딩
            ClassPathResource resource = new ClassPathResource("data/indie_movies_with_actors_directors.json");
            log.info("파일 로딩 완료");
            InputStream inputStream = resource.getInputStream();
            log.info("파일 로딩 완료");

            // 2. JSON 배열 → 객체 리스트로 변환
            List<MovieDocumentRequest> movies = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<MovieDocumentRequest>>() {}
            );
            log.info("객체 리스트 변환 완료: {}", movies.stream().map(movieDocumentRequest -> {return (String) movieDocumentRequest.getMovieId();}));

            // 3. Elasticsearch에 업로드
            for (MovieDocumentRequest movie : movies) {
                try {
                    // movie_ngram 인덱스에 document 저장
                    IndexRequest movieRequest = new IndexRequest("movie_ngram")
                            .id(String.valueOf(movie.getMovieId()))
                            .source(objectMapper.writeValueAsString(movie), XContentType.JSON);

                    esClient.index(movieRequest, RequestOptions.DEFAULT);

                    /*
                    // genre 인덱스에 document 저장
                    if(movie.getGenres() != null){
                        for (MovieDocumentRequest.Genre genre : movie.getGenres()){
                            // 이미 저장된 데이터인지 확인
                            GetRequest getRequest = new GetRequest("genre", genre.getId());
                            boolean exists = esClient.exists(getRequest, RequestOptions.DEFAULT);

                            if(!exists) {
                                IndexRequest genreRequest = new IndexRequest("genre")
                                        .id(String.valueOf(genre.getId()))
                                        .source(objectMapper.writeValueAsString(genre), XContentType.JSON);
                                esClient.index(genreRequest, RequestOptions.DEFAULT);
                            }
                        }
                    }
                    
                    // keyword 인덱스에 keyword 저장
                    if(movie.getKeywords() != null){
                        for(MovieDocumentRequest.Keyword keyword : movie.getKeywords()){
                            // 이미 저장된 데이터인지 확인
                            GetRequest getRequest = new GetRequest("keyword", keyword.getId());
                            boolean exists = esClient.exists(getRequest, RequestOptions.DEFAULT);

                            if(!exists){
                                IndexRequest keywordRequest = new IndexRequest("keyword")
                                        .id(String.valueOf(keyword.getId()))
                                        .source(objectMapper.writeValueAsString(keyword), XContentType.JSON);
                                esClient.index(keywordRequest, RequestOptions.DEFAULT);
                            }
                        }
                    }

                    // maker 인덱스에 저장 (actor)
                    if(movie.getActors() != null ){
                        for(MovieDocumentRequest.Maker actor : movie.getActors()) {
                            // director에 저장된 데이터인지 확인
                            GetRequest getDirectorRequest = new GetRequest("director", actor.getId());
                            boolean existDirector = esClient.exists(getDirectorRequest, RequestOptions.DEFAULT);
                            if (existDirector){
                                actor.setRole("actor_and_director");
                            }

                            // 이미 존재하는 배우(actor)인지 확인
                            GetRequest getActorRequest = new GetRequest("actor", actor.getId());
                            boolean existActor = esClient.exists(getActorRequest, RequestOptions.DEFAULT);
                            if(!existActor) {
                                actor.setRole("actor");
                                IndexRequest actorRequest = new IndexRequest("maker")
                                        .id(String.valueOf(actor.getId()))
                                        .source(objectMapper.writeValueAsString(actor), XContentType.JSON);
                                esClient.index(actorRequest, RequestOptions.DEFAULT);
                            }
                        }
                    }

                    // maker 인덱스에 저장 (director)
                    if(movie.getDirectors() != null ){
                        for(MovieDocumentRequest.Maker director : movie.getDirectors()) {
                            GetRequest getActorRequest = new GetRequest("actor", director.getId());
                            boolean existActor = esClient.exists(getActorRequest, RequestOptions.DEFAULT);
                            if(existActor){
                                director.setRole("actor_and_director");
                            }

                            GetRequest getDirectorRequest = new GetRequest("director", director.getId());
                            boolean existDirector = esClient.exists(getDirectorRequest, RequestOptions.DEFAULT);
                            if(!existDirector) {
                                director.setRole("director");
                                IndexRequest directorRequest = new IndexRequest("maker")
                                        .id(String.valueOf(director.getId()))
                                        .source(objectMapper.writeValueAsString(director), XContentType.JSON);
                                esClient.index(directorRequest, RequestOptions.DEFAULT);
                            }
                        }
                    }
                     */
                } catch (Exception e) {
                    log.error("Elasticsearch 저장 실패: movie_id=" + movie.getMovieId(), e);
                }
            }
            log.info("업로드 완료");
        } catch (IOException e) {
            return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "데이터 입력 중 오류 발생");
        }
        return ApiSuccessResponse.response(ResponseCode.Ok, "데이터 업로드 완료됨", null);
    }
}
