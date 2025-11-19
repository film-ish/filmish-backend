package com.example.knockknock.controller.request;

import com.example.knockknock.controller.domain.NewReview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    private Long indieId;
    private String title;
    private String content;
    private List<MultipartFile> images;

    public NewReview toNewReview(){
        return new NewReview(indieId, title, content, images);
    }
}
