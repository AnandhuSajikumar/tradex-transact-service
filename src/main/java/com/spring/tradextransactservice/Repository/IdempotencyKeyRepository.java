package com.spring.tradextransactservice.Repository;

import com.spring.tradextransactservice.Models.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyValue(String keyValue);
}
