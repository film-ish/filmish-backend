package com.example.knockknock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
    private String headImage;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ADMIN','MAKER','USER') DEFAULT 'USER'")
    private Role role;

    @ColumnDefault("true")
    @Column(columnDefinition = "TINYINT(1)")
    private Boolean active;

    public List<String> getRoles(){
        return Collections.singletonList("ROLE_" + this.role.toString());
    }
    public String getRole(){
        return this.role.toString();
    }
}
