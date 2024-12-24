package com.secor.inventoryservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.secor.inventoryservice.dto.InventoryResponseDto;
import com.secor.inventoryservice.entity.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer
{
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private static final String TOPIC = "inventory-events";
    private static final String OrderTOPIC = "order-events";
    @Autowired //DEPENDENCY INJECTION PROMISE FULFILLED AT RUNTIME
    private KafkaTemplate<String, String> kafkaTemplate ;



    public void publishInventoryDatum(Inventory inventory) throws JsonProcessingException // LOGIN | REGISTER
    {


//
        // convert to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String datum =  objectMapper.writeValueAsString(inventory);
//
        logger.info(String.format("#### -> Producing message ->"));
        this.kafkaTemplate.send(TOPIC, datum);
   }
    public void publishOrderDatum(InventoryResponseDto inventoryResponseDto) throws JsonProcessingException // LOGIN | REGISTER
    {


//
        // convert to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String datum =  objectMapper.writeValueAsString(inventoryResponseDto);
//
        logger.info(String.format("#### -> Producing message ->"));
        this.kafkaTemplate.send(OrderTOPIC, datum);
    }

}
