package com.orderflow.inventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.UUID;
@Component
public class InventoryMessaging {
    private final SqsClient sqs; private final ObjectMapper json; private final ReservationService reservations;
    private final String requestQueue,resultQueue;
    public InventoryMessaging(SqsClient sqs,ObjectMapper json,ReservationService reservations,
      @Value("${queues.request-name}") String requestName,@Value("${queues.result-name}") String resultName){
        this.sqs=sqs;this.json=json;this.reservations=reservations;
        this.requestQueue=sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(requestName).build()).queueUrl();
        this.resultQueue=sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(resultName).build()).queueUrl();
    }
    @Scheduled(fixedDelayString="${queues.poll-delay-ms:1000}")
    public void pollRequests(){
        var messages=sqs.receiveMessage(ReceiveMessageRequest.builder().queueUrl(requestQueue).waitTimeSeconds(1).maxNumberOfMessages(5).build()).messages();
        for(var message:messages){
            try{
                var request=json.readValue(message.body(),OrderRequested.class);
                boolean reserved=reservations.reserve(request.orderId(),request.sku(),request.quantity());
                var result=new OrderResult(request.orderId(),reserved);
                sqs.sendMessage(SendMessageRequest.builder().queueUrl(resultQueue).messageBody(json.writeValueAsString(result)).build());
                sqs.deleteMessage(DeleteMessageRequest.builder().queueUrl(requestQueue).receiptHandle(message.receiptHandle()).build());
            }catch(Exception e){System.err.println("Order request left on queue for retry: "+e.getMessage());}
        }
    }
    public record OrderRequested(UUID orderId,String sku,int quantity){}
    public record OrderResult(UUID orderId,boolean reserved){}
}
