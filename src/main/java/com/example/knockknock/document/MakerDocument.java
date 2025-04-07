package com.example.knockknock.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "maker")
public class MakerDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Date)
    private String birth;

    @Field(type = FieldType.Nested)
    private List<MovieRef> movies;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieRef {
        private String id;
        private String title;
        private Date pubDate;
    }


}
