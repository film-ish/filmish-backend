package com.example.knockknock.controller.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record NewReview(Long indieId, String title, String content, List<MultipartFile> images) {}

