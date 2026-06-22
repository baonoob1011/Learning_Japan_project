package com.example.learningApp.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRetryRedisService {

    private static final Duration RETRY_TTL = Duration.ofMinutes(15);
    private static final Duration TXN_MAP_TTL = Duration.ofDays(1);
    private static final Duration CREATE_LOCK_TTL = Duration.ofSeconds(5);
    private static final Duration CALLBACK_LOCK_TTL = Duration.ofSeconds(20);
    private static final Duration CALLBACK_DONE_TTL = Duration.ofDays(1);

    private static final String RETRY_KEY_PREFIX = "payment:retry:";
    private static final String TXN_MAP_KEY_PREFIX = "payment:txn-map:";
    private static final String CREATE_LOCK_PREFIX = "payment:create-lock:";
    private static final String CALLBACK_LOCK_PREFIX = "payment:callback-lock:";
    private static final String CALLBACK_DONE_PREFIX = "payment:callback-done:";

    private final StringRedisTemplate redisTemplate;

    public boolean acquireCreateLock(String userId, String productType, String productId) {
        String key = CREATE_LOCK_PREFIX + userId + ":" + productType + ":" + productId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", CREATE_LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public void initRetryWindow(String orderId, String userId, String currentOrderCode) {
        long retryUntilEpochMs = Instant.now().plus(RETRY_TTL).toEpochMilli();
        String value = serialize(userId, currentOrderCode, retryUntilEpochMs);
        redisTemplate.opsForValue().set(retryKey(orderId), value, RETRY_TTL);
        redisTemplate.opsForValue().set(txnMapKey(currentOrderCode), orderId, TXN_MAP_TTL);
    }

    public Optional<String> resolveOrderIdByTxnRef(String txnRef) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(txnMapKey(txnRef)));
    }

    public boolean canRetry(String orderId, String userId, String orderCode) {
        return getState(orderId)
                .filter(state -> userId.equals(state.userId()))
                .filter(state -> orderCode.equals(state.currentOrderCode()))
                .filter(state -> Instant.now().toEpochMilli() <= state.retryUntilEpochMs())
                .isPresent();
    }

    public boolean isRetryExpired(String orderId) {
        return getState(orderId)
                .map(state -> Instant.now().toEpochMilli() > state.retryUntilEpochMs())
                .orElse(true);
    }

    public void rotateTxnRef(String orderId, String newOrderCode) {
        RetryState state = getState(orderId).orElse(null);
        if (state == null) {
            return;
        }

        long now = Instant.now().toEpochMilli();
        long remainingMs = state.retryUntilEpochMs() - now;
        if (remainingMs <= 0) {
            redisTemplate.delete(retryKey(orderId));
            return;
        }

        String value = serialize(state.userId(), newOrderCode, state.retryUntilEpochMs());
        redisTemplate.opsForValue().set(retryKey(orderId), value, Duration.ofMillis(remainingMs));
        redisTemplate.opsForValue().set(txnMapKey(newOrderCode), orderId, TXN_MAP_TTL);
    }

    public void markRetryExpired(String orderId) {
        redisTemplate.delete(retryKey(orderId));
    }

    public boolean acquireCallbackLock(String txnRef, String transactionNo) {
        String key = CALLBACK_LOCK_PREFIX + txnRef + ":" + safe(transactionNo);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", CALLBACK_LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    public boolean isCallbackProcessed(String txnRef, String transactionNo) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(callbackDoneKey(txnRef, transactionNo)));
    }

    public void markCallbackProcessed(String txnRef, String transactionNo) {
        redisTemplate.opsForValue().set(callbackDoneKey(txnRef, transactionNo), "1", CALLBACK_DONE_TTL);
    }

    private Optional<RetryState> getState(String orderId) {
        String raw = redisTemplate.opsForValue().get(retryKey(orderId));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            String[] parts = raw.split("\\|", 3);
            if (parts.length != 3) {
                return Optional.empty();
            }
            return Optional.of(new RetryState(parts[0], parts[1], Long.parseLong(parts[2])));
        } catch (Exception ex) {
            log.warn("Invalid retry state for order {}", orderId);
            return Optional.empty();
        }
    }

    private String retryKey(String orderId) {
        return RETRY_KEY_PREFIX + orderId;
    }

    private String txnMapKey(String txnRef) {
        return TXN_MAP_KEY_PREFIX + txnRef;
    }

    private String callbackDoneKey(String txnRef, String transactionNo) {
        return CALLBACK_DONE_PREFIX + txnRef + ":" + safe(transactionNo);
    }

    private String serialize(String userId, String currentOrderCode, long retryUntilEpochMs) {
        return userId + "|" + currentOrderCode + "|" + retryUntilEpochMs;
    }

    private String safe(String value) {
        return value == null ? "NA" : value;
    }

    private record RetryState(String userId, String currentOrderCode, long retryUntilEpochMs) {
    }
}

