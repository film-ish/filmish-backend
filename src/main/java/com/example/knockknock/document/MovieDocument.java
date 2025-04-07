package com.example.knockknock.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "movie_ngram")
public class MovieDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String plot;

    @Field(type = FieldType.Date)
    private Date pubDate;

    @Field(type = FieldType.Integer)
    private Integer runningTime;

    @Field(type = FieldType.Keyword)
    private List<String> genre;

    @Field(type = FieldType.Object)
    private Maker director;

    @Field(type = FieldType.Nested)
    private List<Maker> actors;

    @Field(type = FieldType.Keyword, analyzer = "nori")
    private List<String> keywords;

    @Field(type = FieldType.Float, index = false)
    private float rate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Maker{
        private String id;
        @Field(type = FieldType.Keyword, analyzer = "nori")
        private String name;
        private String role;
        private Date birth;
    }
}