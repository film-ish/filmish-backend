package com.example.knockknock.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(unique = true, length = 100, nullable = false)
    private String password;

    @Column(unique = true, length = 100, nullable = false)
    private String nickname;

    private Date birth;

    private String image;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ADMIN','MAKER','USER') DEFAULT 'USER'")
    private Role role;


    @ColumnDefault("true")
    @Column(columnDefinition = "TINYINT(1)")
    private Boolean active;

    public String getRole(){
        return this.role.toString();
    }
}
