package com.spring.tradextransactservice.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.tradextransactservice.Enums.IdempotencyStatus;
import com.spring.tradextransactservice.Exception.ConcurrentRequestException;
import com.spring.tradextransactservice.Models.IdempotencyKey;
import com.spring.tradextransactservice.Repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyKey createOrReturnKey(String keyString, Long userId) {
        if (keyString == null || keyString.trim().isEmpty()) {
            return null;
        }

        Optional<IdempotencyKey> existingKeyOpt = repository.findByKeyValue(keyString);

        if (existingKeyOpt.isPresent()) {
            IdempotencyKey existingKey = existingKeyOpt.get();

            if (existingKey.getStatus() == IdempotencyStatus.PENDING) {
                log.warn("Concurrent request detected for idempotency key: {}", keyString);
                throw new ConcurrentRequestException("Request is already processing. Please wait.");
            }
            return existingKey;
        }

        IdempotencyKey newKey = IdempotencyKey.create(keyString, userId);
        return repository.save(newKey);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String keyString, Object responsePayload) {
        if (keyString == null || keyString.trim().isEmpty())
            return;

        repository.findByKeyValue(keyString).ifPresent(key -> {
            try {
                String jsonResponse = objectMapper.writeValueAsString(responsePayload);
                key.markCompleted(jsonResponse);
                repository.save(key);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize response for idempotency key: {}", keyString, e);

                key.markCompleted("{\"error\": \"Could not serialize response\"}");
                repository.save(key);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String keyString) {
        if (keyString == null || keyString.trim().isEmpty())
            return;

        repository.findByKeyValue(keyString).ifPresent(key -> {
            key.markFailed();
            repository.save(key);
        });
    }
}
