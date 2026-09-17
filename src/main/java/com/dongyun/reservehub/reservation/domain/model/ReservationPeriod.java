package com.dongyun.reservehub.reservation.domain.model;

import java.time.LocalDateTime;

public record ReservationPeriod(
        LocalDateTime startAt,
        LocalDateTime endAt
) {

    public ReservationPeriod {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException(
                    "예약 시작 시간과 종료 시간은 필수입니다."
            );
        }

        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException(
                    "예약 시작 시간은 종료 시간보다 빨라야 합니다."
            );
        }
    }

    public boolean overlaps(ReservationPeriod other) {
        if (other == null) {
            throw new IllegalArgumentException(
                    "비교할 예약 시간이 필요합니다."
            );
        }

        return startAt.isBefore(other.endAt)
                && other.startAt.isBefore(endAt);
    }
}