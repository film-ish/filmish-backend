package com.example.knockknock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private final String REDIS_BLACK_LIST_KEY = "tokenBlackList";
    /*
        Blacklist에 저장하는 Access token 만료 시간을 24시간으로 설정한 이유
        - [중복 로그인 방지를 위해] 기발행된 Access token을 무효화시킨다고 해도 기발행된 Refresh token을 이용해
            Access token을 재발행 받을 수 있게 돼버리면 중복 로그인 방지 로직에 허점이 발생한다. 따라서 이를 방지하기 위해
            Reissue 로직 수행 시엔 기발행 Access token이 Blacklist에 있는지 조회한 후 조건을 충족하면 Access token을 재발행하도록 하고,
            Blacklist에 Access token을 저장할 때에는 Refresh token 만료 시간인 24시간으로 키 값을 저장한다.
     */
    private static final long REDIS_BLACK_LIST_EXPIRE_TIME = 24*60*60*1000L;

    public void addTokenToList(
            String value){
        redisTemplate.opsForSet().add(
                REDIS_BLACK_LIST_KEY,
                value
        );

        redisTemplate.expire(REDIS_BLACK_LIST_KEY, REDIS_BLACK_LIST_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    public boolean isContainToken(String value){
        return redisTemplate.opsForSet().isMember(REDIS_BLACK_LIST_KEY, value);
    }

    public List<String> getTokenBlackList(){
        return redisTemplate.opsForList().range(REDIS_BLACK_LIST_KEY, 0, -1);
    }

    public void removeToken(String value){
        redisTemplate.opsForList().remove(REDIS_BLACK_LIST_KEY, 0, value);
    }
}
