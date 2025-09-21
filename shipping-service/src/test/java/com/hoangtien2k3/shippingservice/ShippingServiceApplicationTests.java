package com.hoangtien2k3.shippingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.data.mongodb.embedded.version=4.0.12",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class ShippingServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}