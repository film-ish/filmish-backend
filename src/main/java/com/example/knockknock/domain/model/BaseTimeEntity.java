package com.example.knockknock.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name="created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private Instant created_at;

    @LastModifiedDate
    @Column(name="updated_at", columnDefinition = "TIMESTAMP")
    private Instant updated_at;
}
