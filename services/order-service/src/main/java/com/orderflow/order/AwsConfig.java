package com.orderflow.order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import java.net.URI;
@Configuration
public class AwsConfig {
    @Bean SqsClient sqsClient(@Value("${aws.region}") String region, @Value("${aws.endpoint:}") String endpoint) {
        var builder=SqsClient.builder().region(Region.of(region));
        if (!endpoint.isBlank()) builder.endpointOverride(URI.create(endpoint));
        return builder.build();
    }
}
