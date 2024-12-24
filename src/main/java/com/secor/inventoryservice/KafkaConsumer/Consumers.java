package com.secor.inventoryservice.KafkaConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.secor.inventoryservice.dto.InventoryReservationDto;
import com.secor.inventoryservice.service.InventoryServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
@Service
public class Consumers {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private com.secor.inventoryservice.service.InventoryServices InventoryServices;
    Consumers(InventoryServices InventoryServices)
    {
        this.InventoryServices=InventoryServices;
    }
    @KafkaListener(topics = "inventory-reserve-events",groupId = "1")
    public void handleInventoryReserved(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, InventoryReservationDto.class);
        List<InventoryReservationDto> inventoryReservationDtos = objectMapper.readValue(message, listType);


        InventoryServices.reserveInventory(inventoryReservationDtos);
    }

    @KafkaListener(topics = "inventory-unreserve-events",groupId = "1")
    public void handleInventoryUnReserved(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Long orderId = objectMapper.readValue(message, Long.class);


        InventoryServices.unreserveInventory(orderId);
    }

}
