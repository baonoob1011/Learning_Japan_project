package com.example.learningApp.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBridgePublisher {

    private final EventBridgeClient eventBridgeClient;
    private final ObjectMapper objectMapper;

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

            // 3. Gửi đi và HỨNG KẾT QUẢ TRẢ VỀ (Quan trọng!)
            PutEventsResponse response = eventBridgeClient.putEvents(PutEventsRequest.builder()
                    .entries(entry)
                    .build());

            // 4. Kiểm tra xem có vé nào bị trượt (Failed) không
            if (response.failedEntryCount() > 0) {
                // Lấy lỗi đầu tiên ra để soi
                PutEventsResultEntry failure = response.entries().get(0);
                log.error("❌ GỬI THẤT BẠI! Mã lỗi: [{}] - Lý do: [{}]",
                        failure.errorCode(), failure.errorMessage());
            } else {
                // Thành công thật sự
                log.info("✅ Sent EventBridge OK: [{}] type [{}] - ID: {}",
                        source, eventType, response.entries().get(0).eventId());
            }

        } catch (Exception e) {
            // Lỗi này là lỗi code (VD: JSON sai, mất mạng...)
            log.error("❌ Exception sending event: {}", eventType, e);
        }
    }
}