package com.dongyun.reservehub;

import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationPeriodTest {

    private final LocalDateTime baseTime =
            LocalDateTime.of(2026, 10, 1, 10, 0);

    @Test
    void 올바른_예약시간을_생성한다() {
        ReservationPeriod period =
                new ReservationPeriod(baseTime, baseTime.plusHours(1));

        assertEquals(baseTime, period.startAt());
        assertEquals(baseTime.plusHours(1), period.endAt());
    }

    @Test
    void 시작시간과_종료시간이_같으면_생성할수없다() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ReservationPeriod(baseTime, baseTime)
        );
    }

    @Test
    void 시작시간이_종료시간보다_늦으면_생성할수없다() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ReservationPeriod(
                        baseTime.plusHours(1),
                        baseTime
                )
        );
    }

    @Test
    void 두_예약시간이_겹치는지_확인한다() {
        ReservationPeriod first =
                new ReservationPeriod(baseTime, baseTime.plusHours(2));

        ReservationPeriod second =
                new ReservationPeriod(
                        baseTime.plusHours(1),
                        baseTime.plusHours(3)
                );

        assertTrue(first.overlaps(second));
    }

    @Test
    void 앞_예약의_종료와_다음_예약의_시작이_같으면_겹치지않는다() {
        ReservationPeriod first =
                new ReservationPeriod(baseTime, baseTime.plusHours(1));

        ReservationPeriod second =
                new ReservationPeriod(
                        baseTime.plusHours(1),
                        baseTime.plusHours(2)
                );

        assertFalse(first.overlaps(second));
    }
}
