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
class ReservationAvailabilityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepositoryPort repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final UUID resourceId =
            UUID.randomUUID();

    private final LocalDateTime startAt =
            LocalDateTime.of(
                    2031, 10, 1, 10, 0
            );

    private final LocalDateTime endAt =
            LocalDateTime.of(
                    2031, 10, 1, 11, 0
            );

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
    void 겹치는_예약이_없으면_예약가능하다()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/reservations/availability"
                        )
                                .param(
                                        "resourceId",
                                        resourceId.toString()
                                )
                                .param(
                                        "startAt",
                                        startAt.toString()
                                )
                                .param(
                                        "endAt",
                                        endAt.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.available")
                                .value(true)
                );
    }

    @Test
    void 확정된_예약과_겹치면_예약할수없다()
            throws Exception {

        Reservation reservation =
                Reservation.create(
                        UUID.randomUUID(),
                        resourceId,
                        UUID.randomUUID(),
                        new ReservationPeriod(
                                startAt,
                                endAt
                        )
                );

        repository.save(reservation);

        mockMvc.perform(
                        get(
                                "/api/reservations/availability"
                        )
                                .param(
                                        "resourceId",
                                        resourceId.toString()
                                )
                                .param(
                                        "startAt",
                                        startAt.toString()
                                )
                                .param(
                                        "endAt",
                                        endAt.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.available")
                                .value(false)
                );
    }
}