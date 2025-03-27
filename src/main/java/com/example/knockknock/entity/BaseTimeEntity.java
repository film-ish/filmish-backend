package com.example.knockknock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name="created_at", updatable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name="updated_at", columnDefinition = "TIMESTAMP")
    private Instant updatedAt;

    @Column(name="deleted_at", columnDefinition = "TIMESTAMP")
    private Instant deletedAt;

    // 삭제 여부 확인 메서드
    public boolean isSoftDeleted() {
        return deletedAt != null;
    }

    // 삭제 처리 메서드
    protected void deleteSoftly(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    // 삭제 취소 메서드
    protected void undoDeletion() {
        this.deletedAt = null;
    }

}
