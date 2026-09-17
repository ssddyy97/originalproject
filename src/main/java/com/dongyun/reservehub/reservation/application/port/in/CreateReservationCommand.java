package com.dongyun.reservehub.reservation.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReservationCommand(
        UUID resourceId,
        UUID memberId,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
