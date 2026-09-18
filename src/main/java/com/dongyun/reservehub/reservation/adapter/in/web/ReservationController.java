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

import java.util.UUID;
@RestController
@RequestMapping("/api/reservations")

public class ReservationController {
    private final GetReservationUseCase getReservationUseCase;

    private final CreateReservationUseCase createReservationUseCase;

    public ReservationController(
            CreateReservationUseCase createReservationUseCase,
            GetReservationUseCase getReservationUseCase
    ) {
        this.createReservationUseCase =
                createReservationUseCase;
        this.getReservationUseCase =
                getReservationUseCase;
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
}