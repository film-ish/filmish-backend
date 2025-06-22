package com.example.knockknock.repository;

import com.example.knockknock.document.MovieDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Profile("!dev")
public interface MovieDocumentRepository extends ElasticsearchRepository<MovieDocument, String> {
}
