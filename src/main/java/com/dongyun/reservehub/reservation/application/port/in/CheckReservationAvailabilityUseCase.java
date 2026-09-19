package com.dongyun.reservehub.reservation.application.port.in;

import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;

import java.util.UUID;

public interface CheckReservationAvailabilityUseCase {

    boolean isAvailable(
            UUID resourceId,
            ReservationPeriod period
    );
}