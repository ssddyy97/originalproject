package com.dongyun.reservehub.reservation.domain.model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    private final UUID reservationId =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UUID resourceId =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final UUID memberId =
            UUID.fromString("00000000-0000-0000-0000-000000000003");

    private final ReservationPeriod period = new ReservationPeriod(
            LocalDateTime.of(2026, 10, 1, 10, 0),
            LocalDateTime.of(2026, 10, 1, 11, 0)
    );

    @Test
    void 예약을_생성하면_확정상태가_된다() {
        Reservation reservation = Reservation.create(
                reservationId,
                resourceId,
                memberId,
                period
        );

        Assertions.assertEquals(reservationId, reservation.id());
        Assertions.assertEquals(resourceId, reservation.resourceId());
        Assertions.assertEquals(memberId, reservation.memberId());
        Assertions.assertEquals(period, reservation.period());
        assertEquals(
                ReservationStatus.CONFIRMED,
                reservation.status()
        );
    }

    @Test
    void 확정된_예약을_취소할수있다() {
        Reservation reservation = Reservation.create(
                reservationId,
                resourceId,
                memberId,
                period
        );

        reservation.cancel();

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.status()
        );
    }

    @Test
    void 이미_취소된_예약은_다시_취소할수없다() {
        Reservation reservation = Reservation.create(
                reservationId,
                resourceId,
                memberId,
                period
        );

        reservation.cancel();

        Assertions.assertThrows(
                IllegalStateException.class,
                reservation::cancel
        );
    }
}
