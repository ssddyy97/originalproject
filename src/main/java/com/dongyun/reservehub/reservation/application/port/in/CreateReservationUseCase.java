package com.dongyun.reservehub.reservation.application.port.in;

import com.dongyun.reservehub.reservation.domain.model.Reservation;

public interface CreateReservationUseCase {

    Reservation create(CreateReservationCommand command);
}