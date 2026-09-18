package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.in.CancelReservationUseCase;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.ReservationNotFoundException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;

import java.util.Objects;
import java.util.UUID;

public final class CancelReservationService
        implements CancelReservationUseCase {

    private final ReservationRepositoryPort repository;

    public CancelReservationService(
            ReservationRepositoryPort repository
    ) {
        this.repository =
                Objects.requireNonNull(repository);
    }

    @Override
    public Reservation cancelReservation(
            UUID reservationId
    ) {
        Objects.requireNonNull(reservationId);

        Reservation reservation = repository
                .findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                reservationId
                        )
                );

        reservation.cancel();

        return repository.save(reservation);
    }
}