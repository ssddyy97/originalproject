package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.ReservationNotFoundException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelReservationServiceTest {

    @Test
    void 확정된_예약을_취소한다() {
        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.create(
                reservationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new ReservationPeriod(
                        LocalDateTime.of(
                                2030, 10, 1, 10, 0
                        ),
                        LocalDateTime.of(
                                2030, 10, 1, 11, 0
                        )
                )
        );

        FakeReservationRepository repository =
                new FakeReservationRepository(reservation);

        CancelReservationService service =
                new CancelReservationService(repository);

        Reservation result =
                service.cancelReservation(reservationId);

        assertEquals(
                ReservationStatus.CANCELLED,
                result.status()
        );

        assertSame(
                reservation,
                repository.savedReservation
        );
    }

    @Test
    void 존재하지_않는_예약은_취소할_수_없다() {
        UUID reservationId = UUID.randomUUID();

        CancelReservationService service =
                new CancelReservationService(
                        new FakeReservationRepository(null)
                );

        assertThrows(
                ReservationNotFoundException.class,
                () -> service.cancelReservation(
                        reservationId
                )
        );
    }

    private static class FakeReservationRepository
            implements ReservationRepositoryPort {

        private Reservation storedReservation;
        private Reservation savedReservation;

        private FakeReservationRepository(
                Reservation storedReservation
        ) {
            this.storedReservation = storedReservation;
        }

        @Override
        public Optional<Reservation> findById(
                UUID reservationId
        ) {
            if (storedReservation == null) {
                return Optional.empty();
            }

            if (!storedReservation.id()
                    .equals(reservationId)) {
                return Optional.empty();
            }

            return Optional.of(storedReservation);
        }

        @Override
        public Reservation save(
                Reservation reservation
        ) {
            this.storedReservation = reservation;
            this.savedReservation = reservation;

            return reservation;
        }

        @Override
        public boolean existsConfirmedReservationOverlapping(
                UUID resourceId,
                ReservationPeriod period
        ) {
            return false;
        }
    }
}