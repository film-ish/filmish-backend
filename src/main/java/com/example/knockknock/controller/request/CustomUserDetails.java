package com.example.knockknock.controller.request;

import com.example.knockknock.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {
    private final User userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userEntity.getRole();
            }
        });

        return collection;
    }

    public String getUserEmail(){
        return userEntity.getEmail();
    }

    public Long getUserId() {
        return userEntity.getId();
    }

    // UserDetails 인터페이스를 상속받아야 해서 무조건 Overriding 해야 함
    @Override
    public String getUsername(){
        return userEntity.getEmail();
    }

    @Override
    public String getPassword(){
        return userEntity.getPassword();
    }

    public String getBirth(){
        return userEntity.getBirth().toString();
    }

    @Override
    public boolean isAccountNonExpired(){
        //TODO
        // 일단 true 해둔 것!
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    @Override
    public Map<String, Object> getAttributes(){
        return null;
    }

    @Override
    public String getName(){
        return userEntity.getEmail();
    }
}
