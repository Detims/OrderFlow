package com.orderflow.orderservice.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired 
    private MockMvc mockMvc;
    
    @Test 
    void testGetHealth() throws Exception {
        
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("order-service is running."));
    }

    @Test
    void testPostOrder() throws Exception {
        String request = """
                {
                    "sku": "SKU-100",
                    "quantity": 2
                }
                    """;

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value("SKU-100"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test 
    void testPostOrderBlankSku() throws Exception {
        String request = """
                {
                    "sku": "",
                    "quantity": 2
                }
                    """;
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testPostOrderZeroQuantity() throws Exception {
        String request = """
                {
                    "sku": "SKU-100",
                    "quantity": 0
                }
                    """;
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }
}
