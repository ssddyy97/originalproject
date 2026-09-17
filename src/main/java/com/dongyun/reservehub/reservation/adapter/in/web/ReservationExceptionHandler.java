package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.domain.exception.DuplicateReservationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(DuplicateReservationException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateReservation(
            DuplicateReservationException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                "RESERVATION_TIME_CONFLICT",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidArgument(
            IllegalArgumentException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                "INVALID_RESERVATION",
                exception.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + " 값은 필수입니다."
                )
                .orElse("요청 값이 올바르지 않습니다.");

        ApiErrorResponse response = new ApiErrorResponse(
                "INVALID_REQUEST",
                message
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }
}