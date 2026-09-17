package com.dongyun.reservehub.reservation.adapter.in.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationApiConcurrencyTest {

    private static final int REQUEST_COUNT = 10;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID resourceId;

    @AfterEach
    void tearDown() {
        if (resourceId != null) {
            jdbcTemplate.update(
                    """
                    DELETE FROM public.reservations
                    WHERE resource_id = ?
                    """,
                    resourceId
            );
        }
    }

    @Test
    void 동일한_예약을_동시에_요청하면_하나만_성공한다()
            throws Exception {

        resourceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        String requestBody = """
                {
                    "resourceId": "%s",
                    "memberId": "%s",
                    "startAt": "2030-10-01T10:00:00",
                    "endAt": "2030-10-01T11:00:00"
                }
                """.formatted(resourceId, memberId);

        ExecutorService executor =
                Executors.newFixedThreadPool(REQUEST_COUNT);

        CountDownLatch ready =
                new CountDownLatch(REQUEST_COUNT);

        CountDownLatch startSignal =
                new CountDownLatch(1);

        List<Future<Integer>> futures =
                new ArrayList<>();

        try {
            for (int i = 0; i < REQUEST_COUNT; i++) {
                futures.add(
                        executor.submit(() -> {
                            ready.countDown();

                            if (!startSignal.await(
                                    5,
                                    TimeUnit.SECONDS
                            )) {
                                throw new IllegalStateException(
                                        "동시 요청 준비 시간이 초과되었습니다."
                                );
                            }

                            return mockMvc.perform(
                                            post("/api/reservations")
                                                    .contentType(
                                                            MediaType.APPLICATION_JSON
                                                    )
                                                    .content(requestBody)
                                    )
                                    .andReturn()
                                    .getResponse()
                                    .getStatus();
                        })
                );
            }

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "모든 요청이 준비되지 않았습니다."
            );

            startSignal.countDown();

            List<Integer> statuses = new ArrayList<>();

            for (Future<Integer> future : futures) {
                statuses.add(
                        future.get(30, TimeUnit.SECONDS)
                );
            }

            long createdCount = statuses.stream()
                    .filter(status ->
                            status == HttpStatus.CREATED.value()
                    )
                    .count();

            long conflictCount = statuses.stream()
                    .filter(status ->
                            status == HttpStatus.CONFLICT.value()
                    )
                    .count();

            long savedCount = Objects.requireNonNull(
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM public.reservations
                            WHERE resource_id = ?
                            """,
                            Long.class,
                            resourceId
                    )
            );

            assertAll(
                    () -> assertEquals(
                            1,
                            createdCount,
                            "응답 상태: " + statuses
                    ),
                    () -> assertEquals(
                            9,
                            conflictCount,
                            "응답 상태: " + statuses
                    ),
                    () -> assertEquals(
                            1,
                            savedCount
                    )
            );
        } finally {
            startSignal.countDown();
            executor.shutdownNow();
        }
    }
}