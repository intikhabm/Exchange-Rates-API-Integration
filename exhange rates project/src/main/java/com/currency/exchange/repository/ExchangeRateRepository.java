package com.currency.exchange.repository;

import com.currency.exchange.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findByTimestamp(LocalDateTime timestamp);
}
