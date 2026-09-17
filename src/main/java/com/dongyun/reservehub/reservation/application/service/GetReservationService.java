package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.in.GetReservationUseCase;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.ReservationNotFoundException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;

import java.util.Objects;
import java.util.UUID;

public final class GetReservationService
        implements GetReservationUseCase {

    private final ReservationRepositoryPort repository;

    public GetReservationService(
            ReservationRepositoryPort repository
    ) {
        this.repository =
                Objects.requireNonNull(repository);
    }

    @Override
    public Reservation getReservation(UUID reservationId) {
        Objects.requireNonNull(reservationId);

        return repository.findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                reservationId
                        )
                );
    }
}