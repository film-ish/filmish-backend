package com.example.knockknock.controller;

import com.example.knockknock.controller.response.ApiResponse;
import com.example.knockknock.controller.response.ApiSuccessResponse;
import com.example.knockknock.controller.response.ResponseCode;
import com.example.knockknock.error.code.ErrorCode;
import com.example.knockknock.error.response.ApiErrorResponse;
import com.example.knockknock.service.DataUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final DataUploadService dataUploadService;

    @PutMapping("/make-index")
    @Operation(summary = "Elasticsearch 인덱스 생성", description = "Elasticsearch에 인덱스를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 생성함"),
    })
    public ApiResponse makeIndex(){
        RestTemplate restTemplate = new RestTemplate();
        String esUrl = "http://localhost:9200/movie_ngram";
        String jsonBody = """
                {
                  "settings": {
                    "analysis": {
                      "tokenizer": {
                        "ngram_tokenizer": {
                          "type": "nGram",
                          "min_gram": 2,
                          "max_gram": 3,
                          "token_chars": [ "letter", "digit" ]
                        }
                      },
                      "analyzer": {
                        "ngram_analyzer": {
                          "type": "custom",
                          "tokenizer": "ngram_tokenizer",
                          "filter": ["lowercase"]
                        }
                      }
                    }
                  },
                  "mappings": {
                    "properties": {
                      "title": {
                        "type": "text",
                        "analyzer": "ngram_analyzer",
                        "search_analyzer": "standard"
                      }
                    }
                  }
                }
                
                """;

        // 헤더 설정 (Content-Type을 JSON으로)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // body + header를 담은 HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                esUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );
        return ApiSuccessResponse.response(ResponseCode.Created, "인덱스가 성공적으로 생성되었습니다.");
    }



    @GetMapping("/upload-json")
    @Operation(summary = "Elasticsearch 데이터 업로드", description = "데이터를 업로드합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 업로드함"),
    })
    public ApiResponse upload(){
        try {
            dataUploadService.uploadJsonFileToElastic();
        }catch(IOException e){
            return ApiErrorResponse.of(ErrorCode.SERVER_ERROR, "데이터 업로드 중 오류 발생");
        }
        return ApiSuccessResponse.response(ResponseCode.Ok, "JSON 파일 업로드 성공!", null);
    }



}