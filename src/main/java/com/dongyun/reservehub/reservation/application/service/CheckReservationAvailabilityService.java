package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.in.CheckReservationAvailabilityUseCase;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;

import java.util.Objects;
import java.util.UUID;

public final class CheckReservationAvailabilityService
        implements CheckReservationAvailabilityUseCase {

    private final ReservationRepositoryPort repository;

    public CheckReservationAvailabilityService(
            ReservationRepositoryPort repository
    ) {
        this.repository =
                Objects.requireNonNull(repository);
    }

    @Override
    public boolean isAvailable(
            UUID resourceId,
            ReservationPeriod period
    ) {
        Objects.requireNonNull(resourceId);
        Objects.requireNonNull(period);

        return !repository
                .existsConfirmedReservationOverlapping(
                        resourceId,
                        period
                );
    }
}