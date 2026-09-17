package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.ReservationNotFoundException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetReservationServiceTest {

    @Test
    void ID에_해당하는_예약을_반환한다() {
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.create(
                reservationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new ReservationPeriod(
                        LocalDateTime.of(2030, 10, 1, 10, 0),
                        LocalDateTime.of(2030, 10, 1, 11, 0)
                )
        );

        GetReservationService service =
                new GetReservationService(
                        new FakeReservationRepository(
                                reservation
                        )
                );

        Reservation result =
                service.getReservation(reservationId);

        assertSame(reservation, result);
    }

    @Test
    void 예약이_존재하지_않으면_예외가_발생한다() {
        UUID reservationId = UUID.randomUUID();

        GetReservationService service =
                new GetReservationService(
                        new FakeReservationRepository(null)
                );

        assertThrows(
                ReservationNotFoundException.class,
                () -> service.getReservation(reservationId)
        );
    }

    private static class FakeReservationRepository
            implements ReservationRepositoryPort {

        private final Reservation reservation;

        private FakeReservationRepository(
                Reservation reservation
        ) {
            this.reservation = reservation;
        }

        @Override
        public Optional<Reservation> findById(
                UUID reservationId
        ) {
            if (reservation == null) {
                return Optional.empty();
            }

            if (!reservation.id().equals(reservationId)) {
                return Optional.empty();
            }

            return Optional.of(reservation);
        }

        @Override
        public boolean existsConfirmedReservationOverlapping(
                UUID resourceId,
                ReservationPeriod period
        ) {
            return false;
        }

        @Override
        public Reservation save(Reservation reservation) {
            return reservation;
        }
    }
}