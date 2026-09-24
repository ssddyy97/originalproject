package com.dongyun.reservehub.reservation.adapter.out.persistence.jpa;
import java.sql.SQLException;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.domain.exception.DuplicateReservationException;
import com.dongyun.reservehub.reservation.domain.model.Reservation;
import com.dongyun.reservehub.reservation.domain.model.ReservationPeriod;
import com.dongyun.reservehub.reservation.domain.model.ReservationStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.UUID;
import java.util.Optional;

public class JpaReservationRepositoryAdapter
        implements ReservationRepositoryPort {

    private static final String EXCLUSION_VIOLATION = "23P01";

    private final SpringDataReservationJpaRepository repository;

    public JpaReservationRepositoryAdapter(
            SpringDataReservationJpaRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public boolean existsConfirmedReservationOverlapping(
            UUID resourceId,
            ReservationPeriod period
    ) {
        Objects.requireNonNull(resourceId);
        Objects.requireNonNull(period);

        return repository
                .existsByResourceIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
                        resourceId,
                        ReservationStatus.CONFIRMED,
                        period.endAt(),
                        period.startAt()
                );
    }

    @Override
    @Transactional
    public Reservation save(Reservation reservation) {
        Objects.requireNonNull(reservation);
        repository.acquireResourceLock(
                toLockKey(reservation.resourceId())
        );

        try {
            ReservationJpaEntity savedEntity =
                    repository.saveAndFlush(
                            ReservationJpaEntity.fromDomain(reservation)
                    );

            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException exception) {
            if (hasSqlState(exception, "23P01")) {
                throw new DuplicateReservationException();
            }

            throw exception;
        }


    }

    private long toLockKey(UUID resourceId) {
        return resourceId.getMostSignificantBits()
                ^ resourceId.getLeastSignificantBits();
    }

    private boolean containsSqlState(
            Throwable throwable,
            String expectedSqlState
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SQLException sqlException
                    && expectedSqlState.equals(
                    sqlException.getSQLState()
            )) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
    private boolean hasSqlState(
            Throwable throwable,
            String expectedSqlState
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SQLException sqlException
                    && expectedSqlState.equals(sqlException.getSQLState())) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    @Override
    public Optional<Reservation> findById(UUID reservationId) {
        Objects.requireNonNull(reservationId);

        return repository.findById(reservationId)
                .map(ReservationJpaEntity::toDomain);
    }
}