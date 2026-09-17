package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.in.CreateReservationCommand;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.DuplicateReservationException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class CreateReservationServiceTest {

    private final UUID resourceId =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UUID memberId =
            UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void 중복되지_않으면_예약을_생성하고_저장한다() {
        FakeReservationRepository repository =
                new FakeReservationRepository(false);

        CreateReservationService service =
                new CreateReservationService(repository);

        CreateReservationCommand command =
                new CreateReservationCommand(
                        resourceId,
                        memberId,
                        LocalDateTime.of(2026, 10, 1, 10, 0),
                        LocalDateTime.of(2026, 10, 1, 11, 0)
                );

        Reservation reservation = service.create(command);

        assertNotNull(reservation.id());
        assertEquals(resourceId, reservation.resourceId());
        assertEquals(memberId, reservation.memberId());
        assertSame(reservation, repository.savedReservation);
    }

    @Test
    void 시간이_겹치는_예약이_있으면_생성할수없다() {
        FakeReservationRepository repository =
                new FakeReservationRepository(true);

        CreateReservationService service =
                new CreateReservationService(repository);

        CreateReservationCommand command =
                new CreateReservationCommand(
                        resourceId,
                        memberId,
                        LocalDateTime.of(2026, 10, 1, 10, 0),
                        LocalDateTime.of(2026, 10, 1, 11, 0)
                );

        assertThrows(
                DuplicateReservationException.class,
                () -> service.create(command)
        );

        assertNull(repository.savedReservation);
    }

    private static class FakeReservationRepository
            implements ReservationRepositoryPort {

        private final boolean overlapping;
        private Reservation savedReservation;

        private FakeReservationRepository(boolean overlapping) {
            this.overlapping = overlapping;
        }

        @Override
        public boolean existsConfirmedReservationOverlapping(
                UUID resourceId,
                ReservationPeriod period
        ) {
            return overlapping;
        }

        @Override
        public Reservation save(Reservation reservation) {
            this.savedReservation = reservation;
            return reservation;
        }
        @Override
        public Optional<Reservation> findById(UUID reservationId) {
            return Optional.empty();
        }
    }
}
