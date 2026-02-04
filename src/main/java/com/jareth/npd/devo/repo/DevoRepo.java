package com.jareth.npd.devo.repo;

import com.jareth.npd.devo.model.Devo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;



public interface DevoRepo extends JpaRepository<Devo, Long> {
    Optional<Devo> findByDate(LocalDate date);

    List<Devo> findByDateBetween(LocalDate from, LocalDate to);

    List<Devo> findByFechaBetweenOrderByFechaAsc(LocalDate fromDate, LocalDate toDate);
}