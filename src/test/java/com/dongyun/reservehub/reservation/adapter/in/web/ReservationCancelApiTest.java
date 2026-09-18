package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationCancelApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepositoryPort repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final UUID resourceId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    private final LocalDateTime startAt =
            LocalDateTime.of(2026, 10, 2, 10, 0);

    private final LocalDateTime endAt =
            LocalDateTime.of(2026, 10, 2, 11, 0);

    @AfterEach
    void tearDown() {
        jdbcTemplate.update(
                """
                DELETE FROM public.reservations
                WHERE resource_id = ?
                """,
                resourceId
        );
    }

    @Test
    void 예약을_취소하면_같은_시간을_다시_예약할수있다()
            throws Exception {

        UUID reservationId = UUID.randomUUID();

        Reservation reservation = Reservation.create(
                reservationId,
                resourceId,
                memberId,
                new ReservationPeriod(startAt, endAt)
        );

        repository.save(reservation);

        mockMvc.perform(
                        patch(
                                "/api/reservations/{reservationId}/cancel",
                                reservationId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(reservationId.toString())
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );

        Reservation cancelledReservation =
                repository.findById(reservationId)
                        .orElseThrow();

        assertEquals(
                ReservationStatus.CANCELLED,
                cancelledReservation.status()
        );

        String requestBody = """
                {
                  "resourceId": "%s",
                  "memberId": "%s",
                  "startAt": "2026-10-02T10:00:00",
                  "endAt": "2026-10-02T11:00:00"
                }
                """.formatted(resourceId, memberId);

        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                );
    }

    @Test
    void 존재하지않는_예약을_취소하면_404를_반환한다()
            throws Exception {

        UUID unknownReservationId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/reservations/{reservationId}/cancel",
                                unknownReservationId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("RESERVATION_NOT_FOUND")
                );
    }
}