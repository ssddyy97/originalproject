package com.dongyun.reservehub.reservation.domain.exception;

public final class DuplicateReservationException
        extends RuntimeException {

    public DuplicateReservationException() {
        super("선택한 시간에 이미 확정된 예약이 있습니다.");
    }
}