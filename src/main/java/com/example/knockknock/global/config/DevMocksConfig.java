package com.example.knockknock.global.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations; // 이 임포트가 필요합니다.
import static org.mockito.Mockito.mock;

@Configuration
@Profile("dev")
public class DevMocksConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        System.out.println("--- Mock RestHighLevelClient Bean created for dev profile ---");
        return mock(RestHighLevelClient.class);
    }

    @Bean
    public S3Client s3Client() {
        System.out.println("--- Mock S3Client Bean created for dev profile ---");
        return mock(S3Client.class);
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        System.out.println("--- Mock ElasticsearchClient Bean created for dev profile ---");
        return mock(ElasticsearchClient.class);
    }

    /**
     * dev 프로필에서 ElasticsearchOperations (ElasticsearchTemplate)의 Mock 객체를 빈으로 등록합니다.
     * Spring Data Elasticsearch Repository들이 이 빈에 의존합니다.
     */
    @Bean
    public ElasticsearchOperations elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        System.out.println("--- Mock ElasticsearchOperations/Template Bean created for dev profile ---");
        return mock(ElasticsearchOperations.class);
    }

}