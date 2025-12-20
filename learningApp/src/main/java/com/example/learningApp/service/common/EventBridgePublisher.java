package com.example.learningApp.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBridgePublisher {

    private final EventBridgeClient eventBridgeClient;
    private final ObjectMapper objectMapper;

    /**
     * Hàm dùng chung cho TOÀN BỘ hệ thống
     * @param source: Nguồn sự kiện (VD: com.example.auth, com.example.order)
     * @param eventType: Tên sự kiện (VD: UserLogin, OrderCreated)
     * @param payload: Dữ liệu (Object bất kỳ, Map, List, DTO...)
     */
    public void sendEvent(String source, String eventType, Object payload) {
        try {
            // 1. Chuyển Object data thành JSON String
            String jsonDetail = objectMapper.writeValueAsString(payload);

            // 2. Đóng gói
            PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .source(source)
                    .detailType(eventType)
                    .detail(jsonDetail)
                    .eventBusName("default")
                    .build();

            // 3. Gửi đi
            eventBridgeClient.putEvents(PutEventsRequest.builder()
                    .entries(entry)
                    .build());

            log.info("✅ Sent EventBridge: [{}] type [{}]", source, eventType);

        } catch (Exception e) {
            log.error("❌ Failed to send EventBridge event: {}", eventType, e);
        }
    }
}