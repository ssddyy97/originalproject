package com.dongyun.reservehub.reservation.domain.model.adapter.out.persistence.memory;


import com.dongyun.reservehub.reservation.adapter.out.persistence.memory.InMemoryReservationRepositoryAdapter;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryReservationRepositoryAdapterTest {
    private InMemoryReservationRepositoryAdapter repository;

    private final UUID resourceId =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UUID memberId =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final LocalDateTime baseTime =
            LocalDateTime.of(2026, 10, 1, 10, 0);

    @BeforeEach
    void setUp() {
        repository =
                new InMemoryReservationRepositoryAdapter();
    }

    @Test
    void 저장된_예약과_시간이_겹치면_true를_반환한다() {
        Reservation reservation = createReservation(
                baseTime,
                baseTime.plusHours(2)
        );

        repository.save(reservation);

        ReservationPeriod requestedPeriod =
                new ReservationPeriod(
                        baseTime.plusHours(1),
                        baseTime.plusHours(3)
                );

        assertTrue(
                repository.existsConfirmedReservationOverlapping(
                        resourceId,
                        requestedPeriod
                )
        );
    }

    @Test
    void 예약시간이_연속되어도_겹치지않는다() {
        Reservation reservation = createReservation(
                baseTime,
                baseTime.plusHours(1)
        );

        repository.save(reservation);

        ReservationPeriod requestedPeriod =
                new ReservationPeriod(
                        baseTime.plusHours(1),
                        baseTime.plusHours(2)
                );

        assertFalse(
                repository.existsConfirmedReservationOverlapping(
                        resourceId,
                        requestedPeriod
                )
        );
    }

    @Test
    void 취소된_예약은_중복검사에서_제외한다() {
        Reservation reservation = createReservation(
                baseTime,
                baseTime.plusHours(2)
        );

        reservation.cancel();
        repository.save(reservation);

        ReservationPeriod requestedPeriod =
                new ReservationPeriod(
                        baseTime.plusMinutes(30),
                        baseTime.plusHours(1)
                );

        assertFalse(
                repository.existsConfirmedReservationOverlapping(
                        resourceId,
                        requestedPeriod
                )
        );
    }

    private Reservation createReservation(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return Reservation.create(
                UUID.randomUUID(),
                resourceId,
                memberId,
                new ReservationPeriod(startAt, endAt)
        );
    }


}
