package com.dongyun.reservehub.reservation.adapter.in.web;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationQueryApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepositoryPort repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID reservationId;

    @AfterEach
    void tearDown() {
        if (reservationId != null) {
            jdbcTemplate.update(
                    """
                    DELETE FROM public.reservations
                    WHERE id = ?
                    """,
                    reservationId
            );
        }
    }

    @Test
    void 저장된_예약을_ID로_조회한다() throws Exception {
        reservationId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Reservation reservation = Reservation.create(
                reservationId,
                resourceId,
                memberId,
                new ReservationPeriod(
                        LocalDateTime.of(
                                2030, 10, 1, 10, 0
                        ),
                        LocalDateTime.of(
                                2030, 10, 1, 11, 0
                        )
                )
        );

        repository.save(reservation);

        mockMvc.perform(
                        get(
                                "/api/reservations/{reservationId}",
                                reservationId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(reservationId.toString())
                )
                .andExpect(
                        jsonPath("$.resourceId")
                                .value(resourceId.toString())
                )
                .andExpect(
                        jsonPath("$.memberId")
                                .value(memberId.toString())
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                );
    }

    @Test
    void 존재하지_않는_예약은_404를_반환한다()
            throws Exception {

        reservationId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/reservations/{reservationId}",
                                reservationId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("RESERVATION_NOT_FOUND")
                );
    }
}