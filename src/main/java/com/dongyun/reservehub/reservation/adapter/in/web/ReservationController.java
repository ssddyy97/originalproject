package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.application.port.in.CreateReservationUseCase;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dongyun.reservehub.reservation.application.port.in.GetReservationUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.dongyun.reservehub.reservation.application.port.in.CancelReservationUseCase;
import org.springframework.web.bind.annotation.PatchMapping;
import java.util.UUID;
import com.dongyun.reservehub.reservation.application.port.in.CheckReservationAvailabilityUseCase;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api/reservations")

public class ReservationController {
    private final GetReservationUseCase getReservationUseCase;

    private final CreateReservationUseCase createReservationUseCase;

    private final CancelReservationUseCase cancelReservationUseCase;
    private final CheckReservationAvailabilityUseCase checkReservationAvailabilityUseCase;
    public ReservationController(
        CreateReservationUseCase createReservationUseCase,
        GetReservationUseCase getReservationUseCase,
        CancelReservationUseCase cancelReservationUseCase,
        CheckReservationAvailabilityUseCase
                checkReservationAvailabilityUseCase
) {
    this.createReservationUseCase =
            createReservationUseCase;

    this.getReservationUseCase =
            getReservationUseCase;

    this.cancelReservationUseCase =
            cancelReservationUseCase;

    this.checkReservationAvailabilityUseCase =
            checkReservationAvailabilityUseCase;
}
@GetMapping("/availability")
public ResponseEntity<ReservationAvailabilityResponse>
checkAvailability(
        @RequestParam UUID resourceId,

        @RequestParam
        @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE_TIME
        )
        LocalDateTime startAt,

        @RequestParam
        @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE_TIME
        )
        LocalDateTime endAt
) {
    ReservationPeriod period =
            new ReservationPeriod(startAt, endAt);

    boolean available =
            checkReservationAvailabilityUseCase
                    .isAvailable(resourceId, period);

    return ResponseEntity.ok(
            new ReservationAvailabilityResponse(
                    available
            )
    );
}
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Reservation reservation =
                createReservationUseCase.create(
                        request.toCommand()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservation(
            @PathVariable UUID reservationId
    ) {
        Reservation reservation =
                getReservationUseCase.getReservation(
                        reservationId
                );

        return ResponseEntity.ok(
                ReservationResponse.from(reservation)
        );
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable UUID reservationId
    ) {
        Reservation cancelledReservation =
                cancelReservationUseCase.cancelReservation(
                        reservationId
                );

        return ResponseEntity.ok(
                ReservationResponse.from(cancelledReservation)
        );
    }

}