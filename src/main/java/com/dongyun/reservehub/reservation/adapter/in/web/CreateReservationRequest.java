package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.application.port.in.CreateReservationCommand;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReservationRequest(

        @NotNull
        UUID resourceId,

        @NotNull
        UUID memberId,

        @NotNull
        LocalDateTime startAt,

        @NotNull
        LocalDateTime endAt

) {
    public CreateReservationCommand toCommand() {
        return new CreateReservationCommand(
                resourceId,
                memberId,
                startAt,
                endAt
        );
    }
}