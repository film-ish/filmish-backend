package com.example.knockknock.service;

import com.example.knockknock.controller.domain.NewReview;
import com.example.knockknock.controller.request.CustomUserDetails;
import com.example.knockknock.entity.IndieMovie;
import com.example.knockknock.entity.Review;
import com.example.knockknock.entity.ReviewImage;
import com.example.knockknock.entity.User;
import com.example.knockknock.repository.IndieMovieRepository;
import com.example.knockknock.repository.ReviewImageRepository;
import com.example.knockknock.repository.ReviewRepository;
import com.example.knockknock.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReviewCreater {
    ReviewRepository reviewRepository;
    UserRepository userRepository;
    IndieMovieRepository indieMovieRepository;
    ReviewImageRepository reviewImageRepository;

    public ReviewCreater(ReviewRepository reviewRepository, UserRepository userRepository, IndieMovieRepository indieMovieRepository, ReviewImageRepository reviewImageRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.indieMovieRepository = indieMovieRepository;
        this.reviewImageRepository = reviewImageRepository;
    }


    public Review create(NewReview newReview, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        log.info("입력된 userId = " + userId);
        log.info("입력된 indieId = " + newReview.indieId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        IndieMovie indieMovie = indieMovieRepository.findById(newReview.indieId())
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));;

        Review review = Review.builder()
                .title(newReview.title())
                .content(newReview.content())
                .views(0)
                .user(user)
                .indieMovie(indieMovie)
                .createdAt(Instant.now())
                .build();

        return reviewRepository.save(review);
    }

    public List<ReviewImage> createReviewImage(List<ReviewImage> reviewImages){
        return reviewImageRepository.saveAll(reviewImages);
    }

}
