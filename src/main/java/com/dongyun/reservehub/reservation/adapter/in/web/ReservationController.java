package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.application.port.in.CreateReservationUseCase;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;

    public ReservationController(
            CreateReservationUseCase createReservationUseCase
    ) {
        this.createReservationUseCase =
                createReservationUseCase;
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
}