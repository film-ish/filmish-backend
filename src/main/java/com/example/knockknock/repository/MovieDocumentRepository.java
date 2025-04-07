package com.example.knockknock.repository;

import com.example.knockknock.document.MovieDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieDocumentRepository extends ElasticsearchRepository<MovieDocument, String> {
}
