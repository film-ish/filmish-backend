package com.example.knockknock.repository;

import com.example.knockknock.document.MovieDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public interface MovieDocumentRepository extends ElasticsearchRepository<MovieDocument, String> {
}
