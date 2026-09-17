package com.dongyun.reservehub.reservation.application.service;

import com.dongyun.reservehub.reservation.application.port.in.CreateReservationCommand;
import com.dongyun.reservehub.reservation.application.port.in.CreateReservationUseCase;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.DuplicateReservationException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;

import java.util.Objects;
import java.util.UUID;

public final class CreateReservationService
        implements CreateReservationUseCase {

    private final ReservationRepositoryPort repositoryPort;

    public CreateReservationService(
            ReservationRepositoryPort repositoryPort
    ) {
        this.repositoryPort =
                Objects.requireNonNull(repositoryPort);
    }

    @Override
    public Reservation create(CreateReservationCommand command) {
        Objects.requireNonNull(command);

        ReservationPeriod period = new ReservationPeriod(
                command.startAt(),
                command.endAt()
        );

        boolean overlapping =
                repositoryPort.existsConfirmedReservationOverlapping(
                        command.resourceId(),
                        period
                );

        if (overlapping) {
            throw new DuplicateReservationException();
        }

        Reservation reservation = Reservation.create(
                UUID.randomUUID(),
                command.resourceId(),
                command.memberId(),
                period
        );

        return repositoryPort.save(reservation);
    }
}