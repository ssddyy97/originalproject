package com.dongyun.reservehub.reservation.adapter.out.persistence.jpa;

import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationJpaEntityTest {

    @Test
    void 도메인_예약을_JPA엔티티로_변환한_후_복원한다() {
        UUID id = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Reservation original = Reservation.create(
                id,
                resourceId,
                memberId,
                new ReservationPeriod(
                        LocalDateTime.of(2026, 10, 1, 10, 0),
                        LocalDateTime.of(2026, 10, 1, 11, 0)
                )
        );

        original.cancel();

        ReservationJpaEntity entity =
                ReservationJpaEntity.fromDomain(original);

        Reservation restored = entity.toDomain();

        assertEquals(original.id(), restored.id());
        assertEquals(original.resourceId(), restored.resourceId());
        assertEquals(original.memberId(), restored.memberId());
        assertEquals(original.period(), restored.period());
        assertEquals(
                ReservationStatus.CANCELLED,
                restored.status()
        );
    }
}