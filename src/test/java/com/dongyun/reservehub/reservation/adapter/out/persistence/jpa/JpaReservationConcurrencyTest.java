package com.dongyun.reservehub.reservation.adapter.out.persistence.jpa;

import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.DuplicateReservationException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JpaReservationConcurrencyTest {

    @Autowired
    private ReservationRepositoryPort reservationRepository;

    @Autowired
    private SpringDataReservationJpaRepository jpaRepository;

    @Test
    void 같은_자원과_시간의_동시예약은_하나만_성공한다()
            throws Exception {

        int requestCount = 10;

        UUID resourceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        ReservationPeriod period = new ReservationPeriod(
                LocalDateTime.of(2030, 1, 1, 10, 0),
                LocalDateTime.of(2030, 1, 1, 11, 0)
        );

        List<UUID> reservationIds = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            reservationIds.add(UUID.randomUUID());
        }

        ExecutorService executor =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch ready =
                new CountDownLatch(requestCount);

        CountDownLatch startSignal =
                new CountDownLatch(1);

        List<Future<Boolean>> results =
                new ArrayList<>();

        try {
            for (UUID reservationId : reservationIds) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    startSignal.await();

                    Reservation reservation =
                            Reservation.create(
                                    reservationId,
                                    resourceId,
                                    memberId,
                                    period
                            );

                    try {
                        reservationRepository.save(reservation);
                        return true;
                    } catch (DuplicateReservationException exception) {
                        return false;
                    }
                }));
            }

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS)
            );

            startSignal.countDown();

            long successCount = 0;

            for (Future<Boolean> result : results) {
                if (result.get(15, TimeUnit.SECONDS)) {
                    successCount++;
                }
            }

            assertEquals(1, successCount);
            assertEquals(
                    1,
                    jpaRepository.countByResourceId(resourceId)
            );
        } finally {
            startSignal.countDown();
            executor.shutdownNow();

            jpaRepository.deleteAllById(reservationIds);
        }
    }
}