package com.example.knockknock.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image.resize")
@Getter
@Setter
public class ImageResizeConfig {
    private Dimension basic;
    private Dimension thumbnail;
    private Dimension profile;

    @Getter
    @Setter
    public static class Dimension {
        private int width;
        private int height;
    }
}