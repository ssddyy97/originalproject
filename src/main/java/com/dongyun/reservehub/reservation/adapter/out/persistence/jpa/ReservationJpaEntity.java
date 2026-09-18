package com.dongyun.reservehub.reservation.adapter.out.persistence.jpa;

import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    protected ReservationJpaEntity() {
        // JPA가 객체를 생성할 때 사용하는 생성자
    }

    private ReservationJpaEntity(
            UUID id,
            UUID resourceId,
            UUID memberId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            ReservationStatus status
    ) {
        this.id = id;
        this.resourceId = resourceId;
        this.memberId = memberId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
    }

    public static ReservationJpaEntity fromDomain(
            Reservation reservation
    ) {
        return new ReservationJpaEntity(
                reservation.id(),
                reservation.resourceId(),
                reservation.memberId(),
                reservation.period().startAt(),
                reservation.period().endAt(),
                reservation.status()
        );
    }

    public Reservation toDomain() {
        return Reservation.restore(
                id,
                resourceId,
                memberId,
                new ReservationPeriod(startAt, endAt),
                status
        );
    }
}