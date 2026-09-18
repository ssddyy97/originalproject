package com.dongyun.reservehub.reservation.adapter.out.persistence.jpa;

import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SpringDataReservationJpaRepository
        extends JpaRepository<ReservationJpaEntity, UUID> {

    boolean existsByResourceIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            UUID resourceId,
            ReservationStatus status,
            LocalDateTime requestedEndAt,
            LocalDateTime requestedStartAt
    );
    long countByResourceId(UUID resourceId);


    @Query(
            value = "SELECT pg_advisory_xact_lock(:lockKey)",
            nativeQuery = true
    )
    void acquireResourceLock(
            @Param("lockKey") long lockKey
    );
}