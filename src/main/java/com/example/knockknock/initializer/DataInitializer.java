//package com.example.knockknock.initializer;
//
//import com.example.knockknock.entity.IndieMovie;
//import com.example.knockknock.repository.IndieMovieRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Component class DataInitializer implements CommandLineRunner {
//    private final IndieMovieRepository indiemovieRepository;
//    private final ObjectMapper objectMapper;
//    public DataInitializer(IndieMovieRepository indiemovieRepository, ObjectMapper objectMapper) {
//        this.indiemovieRepository = indiemovieRepository;
//        this.objectMapper = objectMapper;
//}    @Override
//    public void run(String... args) throws Exception {
//        if (indiemovieRepository.count() == 0) { // 중복 방지
//            Resource resource = new ClassPathResource("data/indie_movies_with_actors_directors.json");
//            List<IndieMovie> movies = Arrays.asList(
//                    objectMapper.readValue(resource.getInputStream(), IndieMovie[].class)
//            );
//            indiemovieRepository.saveAll(movies);
//            System.out.println("✅ 초기 데이터 로딩 완료: " + movies.size() + "건");
//        }
//    }
//}
