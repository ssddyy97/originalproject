package com.dongyun.reservehub.reservation.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class Reservation {

    private final UUID id;
    private final UUID resourceId;
    private final UUID memberId;
    private final ReservationPeriod period;
    private ReservationStatus status;

    private Reservation(
            UUID id,
            UUID resourceId,
            UUID memberId,
            ReservationPeriod period,
            ReservationStatus status
    ) {
        this.id = Objects.requireNonNull(id);
        this.resourceId = Objects.requireNonNull(resourceId);
        this.memberId = Objects.requireNonNull(memberId);
        this.period = Objects.requireNonNull(period);
        this.status = Objects.requireNonNull(status);
    }

    public static Reservation create(
            UUID id,
            UUID resourceId,
            UUID memberId,
            ReservationPeriod period
    ) {
        return new Reservation(
                id,
                resourceId,
                memberId,
                period,
                ReservationStatus.CONFIRMED
        );
    }

    public static Reservation restore(
            UUID id,
            UUID resourceId,
            UUID memberId,
            ReservationPeriod period,
            ReservationStatus status
    ) {
        return new Reservation(
                id,
                resourceId,
                memberId,
                period,
                status
        );
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        status = ReservationStatus.CANCELLED;
    }

    public UUID id() {
        return id;
    }

    public UUID resourceId() {
        return resourceId;
    }

    public UUID memberId() {
        return memberId;
    }

    public ReservationPeriod period() {
        return period;
    }

    public ReservationStatus status() {
        return status;
    }
}