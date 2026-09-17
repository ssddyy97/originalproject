package com.dongyun.reservehub.reservation.adapter.in.web;

public record ApiErrorResponse(
        String code,
        String message
) {
}