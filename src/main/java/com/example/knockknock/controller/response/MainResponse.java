package com.example.knockknock.controller.response;

import lombok.*;

import java.util.List;

@Data
public class MainResponse {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AllList<L, F>{
        private List<ReviewResponse.Detail> bestReviews;
        private List<IndieResponse.StillcutDetail> orderByPubdate;
        private List<IndieResponse.LikeDetail<L>> orderByLikes;
        private List<IndieResponse.LikeDetail<F>> orderByAvg;

        public static <L, F> AllList<L, F> of(List<ReviewResponse.Detail> bestReviews,
                                 List<IndieResponse.StillcutDetail> orderByPubdate,
                                 List<IndieResponse.LikeDetail<L>> orderByLikes,
                                 List<IndieResponse.LikeDetail<F>> orderByAvg){
            return AllList.<L, F>builder()
                    .bestReviews(bestReviews)
                    .orderByPubdate(orderByPubdate)
                    .orderByLikes(orderByLikes)
                    .orderByAvg(orderByAvg)
                    .build();
        }
    }
}
