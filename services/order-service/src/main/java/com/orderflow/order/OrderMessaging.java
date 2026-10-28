package com.orderflow.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
public class OrderMessaging {
    private final SqsClient sqs; private final ObjectMapper json; private final OrderResultHandler resultHandler;
    private final String requestQueue; private final String resultQueue;
    public OrderMessaging(SqsClient sqs, ObjectMapper json, OrderResultHandler resultHandler,
        @Value("${queues.request-name}") String requestName, @Value("${queues.result-name}") String resultName) {
        this.sqs=sqs; this.json=json; this.resultHandler=resultHandler;
        this.requestQueue=sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(requestName).build()).queueUrl();
        this.resultQueue=sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(resultName).build()).queueUrl();
    }
    public void send(OrderRequested event) {
        try { sqs.sendMessage(SendMessageRequest.builder().queueUrl(requestQueue).messageBody(json.writeValueAsString(event)).build()); }
        catch (Exception e) { throw new IllegalStateException("Could not publish order request", e); }
    }
    @Scheduled(fixedDelayString = "${queues.poll-delay-ms:1000}")
    public void pollResults() {
        var messages=sqs.receiveMessage(ReceiveMessageRequest.builder().queueUrl(resultQueue).waitTimeSeconds(1).maxNumberOfMessages(5).build()).messages();
        for (var m: messages) {
            try {
                var result=json.readValue(m.body(), OrderResult.class); resultHandler.apply(result);
                sqs.deleteMessage(DeleteMessageRequest.builder().queueUrl(resultQueue).receiptHandle(m.receiptHandle()).build());
            } catch (Exception e) { System.err.println("Order result left on queue for retry: " + e.getMessage()); }
        }
    }
    public record OrderRequested(UUID orderId, String sku, int quantity) {}
    public record OrderResult(UUID orderId, boolean reserved) {}
}
