package com.example.knockknock.controller.response;

import com.example.knockknock.entity.Maker;
import com.example.knockknock.entity.Type;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;

@Data
public class MainResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AllList{
        private List<ReviewResponse.Detail> bestReviews;
        private List<IndieResponse.Approximate> latest;
        private List<IndieResponse.LikeDetail> orderByLikes;
        private List<IndieResponse.LikeDetail> orderByAvg;

        public static AllList of(List<ReviewResponse.Detail> bestReviews,
                                 List<IndieResponse.Approximate> latest,
                                 List<IndieResponse.LikeDetail> orderByLikes,
                                 List<IndieResponse.LikeDetail> orderByAvg){
            return AllList.builder()
                    .bestReviews(bestReviews)
                    .latest(latest)
                    .orderByLikes(orderByLikes)
                    .orderByAvg(orderByAvg)
                    .build();
        }
    }
}
