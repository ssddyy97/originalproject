package com.dongyun.reservehub.reservation.application.port.in;

import com.dongyun.reservehub.reservation.domain.model.Reservation;

import java.util.UUID;

public interface GetReservationUseCase {

    Reservation getReservation(UUID reservationId);
}