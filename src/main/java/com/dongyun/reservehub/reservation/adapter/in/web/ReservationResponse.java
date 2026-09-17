package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID resourceId,
        UUID memberId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ReservationStatus status
) {

    public static ReservationResponse from(
            Reservation reservation
    ) {
        return new ReservationResponse(
                reservation.id(),
                reservation.resourceId(),
                reservation.memberId(),
                reservation.period().startAt(),
                reservation.period().endAt(),
                reservation.status()
        );
    }
}