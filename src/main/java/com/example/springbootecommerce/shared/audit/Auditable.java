package com.example.springbootecommerce.shared.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base abstracta para entidades auditables.
 * Proporciona campos comunes para tracking de creación y modificación.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    protected String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    protected String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime updatedAt;
}
