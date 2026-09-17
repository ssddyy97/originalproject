package com.dongyun.reservehub.reservation.adapter.config;

import com.dongyun.reservehub.reservation.adapter.out.persistence.jpa.JpaReservationRepositoryAdapter;
import com.dongyun.reservehub.reservation.adapter.out.persistence.jpa.SpringDataReservationJpaRepository;
import com.dongyun.reservehub.reservation.application.port.in.CreateReservationUseCase;
import com.dongyun.reservehub.reservation.application.port.out.ReservationRepositoryPort;
import com.dongyun.reservehub.reservation.application.service.CreateReservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationConfiguration {

    @Bean
    public ReservationRepositoryPort reservationRepositoryPort(
            SpringDataReservationJpaRepository jpaRepository
    ) {
        return new JpaReservationRepositoryAdapter(jpaRepository);
    }

    @Bean
    public CreateReservationUseCase createReservationUseCase(
            ReservationRepositoryPort repositoryPort
    ) {
        return new CreateReservationService(repositoryPort);
    }
}