package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class User {
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(unique = true, length = 100, nullable = false)
    private String password;

    @Column(unique = true, length = 100, nullable = false)
    private String nickname;

    private LocalDate birth;

    private String image;

    @ColumnDefault("USER")
    private Role role;

    @ColumnDefault("true")
    @Column(columnDefinition = "TINYINT(1)")
    private Boolean active;

    public String getRole(){
        return this.role.toString();
    }
}
