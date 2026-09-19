package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckReservationAvailabilityServiceTest {

    @Test
    void 확정된_예약과_겹치면_예약할수없다() {
        ReservationRepositoryPort repository =
                mock(ReservationRepositoryPort.class);

        UUID resourceId = UUID.randomUUID();

        ReservationPeriod period =
                createPeriod();

        when(
                repository
                        .existsConfirmedReservationOverlapping(
                                resourceId,
                                period
                        )
        ).thenReturn(true);

        CheckReservationAvailabilityService service =
                new CheckReservationAvailabilityService(
                        repository
                );

        boolean available =
                service.isAvailable(resourceId, period);

        assertFalse(available);
    }

    @Test
    void 겹치는_예약이_없으면_예약할수있다() {
        ReservationRepositoryPort repository =
                mock(ReservationRepositoryPort.class);

        UUID resourceId = UUID.randomUUID();

        ReservationPeriod period =
                createPeriod();

        when(
                repository
                        .existsConfirmedReservationOverlapping(
                                resourceId,
                                period
                        )
        ).thenReturn(false);

        CheckReservationAvailabilityService service =
                new CheckReservationAvailabilityService(
                        repository
                );

        boolean available =
                service.isAvailable(resourceId, period);

        assertTrue(available);
    }

    private ReservationPeriod createPeriod() {
        return new ReservationPeriod(
                LocalDateTime.of(
                        2030, 10, 1, 10, 0
                ),
                LocalDateTime.of(
                        2030, 10, 1, 11, 0
                )
        );
    }
}