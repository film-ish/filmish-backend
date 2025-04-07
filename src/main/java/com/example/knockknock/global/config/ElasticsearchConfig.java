package com.example.knockknock.global.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
@Slf4j
public class ElasticsearchConfig {
    @Value("${spring.elasticsearch.username}") // 환경 변수에서 사용자 이름 로드 (기본값: 빈 문자열)
    private String username;

    @Value("${spring.elasticsearch.password}") // 환경 변수에서 비밀번호 로드 (기본값: 빈 문자열)
    private String password;

    @Value("${spring.elasticsearch.uris}") // 환경 변수에서 Elasticsearch 호스트 주소 로드
    private String esHost;

    @PostConstruct
    public void printConfig() {
        System.out.println("✅ Elasticsearch URI: " + esHost);
        System.out.println("✅ Username: " + username);
    }

    @Bean
    public RestHighLevelClient esClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(
                new HttpHost("elasticsearch", 9200, "http"))
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        return new RestHighLevelClient(builder);
    }
}
