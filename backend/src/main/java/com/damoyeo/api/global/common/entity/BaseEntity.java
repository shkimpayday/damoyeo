package com.damoyeo.api.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * 모든 엔티티의 공통 부모 클래스 (상속용)
 * ============================================================================
 *
 * [사용 목적]
 * 모든 테이블에 공통으로 들어가는 createdAt, modifiedAt 컬럼을 한 곳에서 관리합니다.
 * Member, Group, Meeting 등 모든 엔티티가 이 클래스를 상속받습니다.
 *
 * [사용 예시]
 *   public class Member extends BaseEntity { ... }
 *   → Member 테이블에 created_at, modified_at 컬럼이 자동으로 추가됨
 *
 * ▶ @MappedSuperclass
 *   - 이 클래스는 실제 테이블로 생성되지 않습니다.
 *   - 대신 이 클래스를 상속받은 자식 엔티티에 필드가 포함됩니다.
 *   - 즉, BaseEntity 테이블은 없고, Member 테이블에 created_at, modified_at이 들어감
 *
 * ▶ @EntityListeners(AuditingEntityListener.class)
 *   - JPA Auditing 기능을 이 엔티티에 적용합니다.
 *   - 엔티티가 저장/수정될 때 자동으로 시간을 기록해줍니다.
 *   - DamoyeoApplication의 @EnableJpaAuditing과 함께 사용해야 동작합니다.
 *
 * ▶ abstract class
 *   - 추상 클래스이므로 직접 new BaseEntity() 할 수 없습니다.
 *   - 반드시 상속받아서 사용해야 합니다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    /**
     * 생성 일시
     *
     * @CreatedDate: 엔티티가 처음 저장될 때 자동으로 현재 시간이 입력됩니다.
     * @Column(updatable = false): 한 번 저장된 후에는 수정할 수 없습니다.
     *
     * [DB 컬럼 예시]
     * created_at DATETIME NOT NULL
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     *
     * @LastModifiedDate: 엔티티가 수정될 때마다 자동으로 현재 시간으로 업데이트됩니다.
     *
     * [DB 컬럼 예시]
     * modified_at DATETIME
     */
    @LastModifiedDate
    private LocalDateTime modifiedAt;
}
