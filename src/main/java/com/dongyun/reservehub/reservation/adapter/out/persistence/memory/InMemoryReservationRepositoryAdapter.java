package com.dongyun.reservehub.reservation.adapter.out.persistence.memory;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
public final class InMemoryReservationRepositoryAdapter
        implements ReservationRepositoryPort {

    private final Map<UUID, Reservation> reservations =
            new ConcurrentHashMap<>();

    @Override
    public boolean existsConfirmedReservationOverlapping(
            UUID resourceId,
            ReservationPeriod period
    ) {
        Objects.requireNonNull(resourceId);
        Objects.requireNonNull(period);


        return reservations.values()
                .stream()
                .filter(reservation ->
                        reservation.status()
                                == ReservationStatus.CONFIRMED
                )
                .filter(reservation ->
                        reservation.resourceId().equals(resourceId)
                )
                .anyMatch(reservation ->
                        reservation.period().overlaps(period)
                );
    }

    @Override
    public Reservation save(Reservation reservation) {
        Objects.requireNonNull(reservation);

        reservations.put(
                reservation.id(),
                reservation
        );

        return reservation;
    }
    public Optional<Reservation> findById(UUID reservationId) {
        Objects.requireNonNull(reservationId);
        System.out.println();
        return Optional.ofNullable(
                reservations.get(reservationId)
        );
    }
}