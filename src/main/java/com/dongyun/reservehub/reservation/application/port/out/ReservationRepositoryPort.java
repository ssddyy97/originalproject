package com.dongyun.reservehub.reservation.application.port.out;

import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {

    boolean existsConfirmedReservationOverlapping(
            UUID resourceId,
            ReservationPeriod period

    );

    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID reservationId);
}