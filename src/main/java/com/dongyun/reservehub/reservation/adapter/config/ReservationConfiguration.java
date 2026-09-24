package com.dongyun.reservehub.reservation.adapter.config;
import com.dongyun.reservehub.reservation.application.port.in.GetReservationUseCase;
import com.dongyun.reservehub.reservation.application.service.GetReservationService;
import com.dongyun.reservehub.reservation.adapter.out.persistence.jpa.JpaReservationRepositoryAdapter;
import com.dongyun.reservehub.reservation.adapter.out.persistence.jpa.SpringDataReservationJpaRepository;
import com.dongyun.reservehub.reservation.application.port.in.CreateReservationUseCase;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.application.service.CreateReservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.dongyun.reservehub.reservation.application.port.in.CancelReservationUseCase;
import com.dongyun.reservehub.reservation.application.service.CancelReservationService;
import com.dongyun.reservehub.reservation.application.port.in.CheckReservationAvailabilityUseCase;
import com.dongyun.reservehub.reservation.application.service.CheckReservationAvailabilityService;
@Configuration
public class ReservationConfiguration {
    @Bean
    public ReservationRepositoryPort reservationRepositoryPort(
            SpringDataReservationJpaRepository jpaRepository
    ) {
        return new JpaReservationRepositoryAdapter(jpaRepository);
    }

    @Bean
    public GetReservationUseCase getReservationUseCase(
            ReservationRepositoryPort reservationRepositoryPort
    ) {
        return new GetReservationService(
                reservationRepositoryPort
        );
    }

    @Bean
    public CreateReservationUseCase createReservationUseCase(
            ReservationRepositoryPort repositoryPort
    ) {
        return new CreateReservationService(repositoryPort);
    }

    @Bean
    public CancelReservationUseCase cancelReservationUseCase(
            ReservationRepositoryPort reservationRepositoryPort
    ) {
        return new CancelReservationService(
                reservationRepositoryPort
        );
    }
    @Bean
public CheckReservationAvailabilityUseCase
checkReservationAvailabilityUseCase(
        ReservationRepositoryPort reservationRepositoryPort
) {
    return new CheckReservationAvailabilityService(
            reservationRepositoryPort
    );
}
}