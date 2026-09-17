package com.dongyun.reservehub.reservation.domain.exception;

import java.util.UUID;

public final class ReservationNotFoundException
        extends RuntimeException {

    public ReservationNotFoundException(UUID reservationId) {
        super(
                "예약을 찾을 수 없습니다. reservationId="
                        + reservationId
        );
    }
}