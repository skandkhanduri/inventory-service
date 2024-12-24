package com.secor.inventoryservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.secor.inventoryservice.config.KafkaProducer;
import com.secor.inventoryservice.config.RequestIdGenerator;
import com.secor.inventoryservice.dto.InventoryReservationDto;
import com.secor.inventoryservice.dto.InventoryResponseDto;
import com.secor.inventoryservice.dto.ProductDto;
import com.secor.inventoryservice.entity.Inventory;
import com.secor.inventoryservice.entity.InventoryReservation;
import com.secor.inventoryservice.mapper.InventoryMapper;
import com.secor.inventoryservice.repository.InventoryRepository;
import com.secor.inventoryservice.repository.InventoryReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class InventoryServices {
    private static final Logger log = LoggerFactory.getLogger(InventoryServices.class);

    InventoryRepository inventoryRepository;
    InventoryReservationRepository inventoryReservationRepository;
    WebClient productWebClient;

    KafkaProducer kafkaProducer;

    InventoryServices(InventoryRepository inventoryRepository,
                      InventoryReservationRepository inventoryReservationRepository,
                      WebClient productWebClient,
                      KafkaProducer kafkaProducer) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.productWebClient = productWebClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<Inventory> getAllInventory() {

        return inventoryRepository.findAll();
    }

    public Optional<Inventory> getInventoryById(Long id) {
        Optional<Inventory> inventory = inventoryRepository.findInventoryByProductId(id);
        if (inventory.isPresent()) {
            return inventory;
        } else {
            throw new EntityNotFoundException("Customer with id " + id + " not found");
        }
    }

    public InventoryResponseDto createInventory(Inventory inventory) {
        Mono<ProductDto> response = productWebClient.get().uri(uriBuilder ->
                        uriBuilder.queryParam("productId", inventory.getProductId()).build())
                .retrieve().bodyToMono(ProductDto.class);
        String requestId = RequestIdGenerator.generateRequestId();


        response.subscribe(
                productResponse -> {
                    log.info("validating product response");
                    if (productResponse != null && productResponse.getProductId() != null) {
                        log.info("product validated now proceeding with insertion logic ");
                        inventory.setRequestId(requestId);
                        inventory.setCreatedAt(LocalDateTime.now());
                        inventory.setUpdatedAt(LocalDateTime.now());
                        Inventory savedInventory = inventoryRepository.save(inventory);
                        try {
                            kafkaProducer.publishInventoryDatum(savedInventory);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        //Product doesn't exist, handle the error
                        log.info("Product not found. Cannot add inventory.");
                    }

                },
                error -> {
                    // Handle error from WebClient call (e.g., if product service is down)
                    log.error("Error occurred while retrieving product details: " + error.getMessage());
                }


        );
        return new InventoryResponseDto(
                "Inventory addition request received successfully", requestId, "Success");
    }

    public Inventory updateInventory(Inventory inventoryDetails) {
        Optional<Inventory> optionalProduct = inventoryRepository.findInventoryByProductId(inventoryDetails.getProductId());
        if (optionalProduct.isPresent()) {
            inventoryDetails.setRequestId(optionalProduct.get().getRequestId());
            inventoryDetails.setCreatedAt(optionalProduct.get().getCreatedAt());
            inventoryDetails.setUpdatedAt(LocalDateTime.now());
            return inventoryRepository.save(inventoryDetails);
        } else {
            throw new EntityNotFoundException("Inventory with id " + inventoryDetails.getProductId() + " not found");
        }
    }

    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    public InventoryResponseDto getInventoryStatus(String requestId) {
        Optional<Inventory> inventory = inventoryRepository.findInventoryByRequestId(requestId);
        if (inventory.isPresent()) {
            return new InventoryResponseDto(
                    "Inventory addition request has been completed successfully", requestId, "Completed");
        } else {
            return new InventoryResponseDto(
                    "Inventory addition Failed as product does not exists in catalogue", requestId, "Failed");
        }
    }

    public InventoryResponseDto checkInventoryQuantity(Long productId, Integer quantity) {
        Optional<Inventory> optionalProduct = inventoryRepository.findInventoryByProductId(productId);
        if (optionalProduct.isPresent()) {
            Integer availableQuantity = optionalProduct.get().getQuantity();
            String requestId = optionalProduct.get().getRequestId();
            if (availableQuantity > quantity) {
                return new InventoryResponseDto(
                        "Inventory available", requestId, "Available");

            } else {
                return new InventoryResponseDto(
                        "Inventory quantity is not available", requestId, "Not-Available");

            }
        } else {
            return new InventoryResponseDto(
                    "Product not available", RequestIdGenerator.generateRequestId(), "Product-Not-Available");

        }
    }
/*
    public InventoryReservationDto reserveInventory(InventoryReservationDto inventoryReservationDto) {

        Integer reservationQuantity = inventoryReservationDto.getQuantityReserved();
        Long productId = inventoryReservationDto.getProductId();
        Inventory inventory = inventoryRepository.findInventoryByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        Integer inventoryQuantity = inventory.getQuantity();
        if (inventoryQuantity < reservationQuantity) {
            throw new RuntimeException("Insufficient inventory for product: " + productId);
        }
        inventory.setQuantity(inventoryQuantity - reservationQuantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + reservationQuantity);
        inventoryRepository.save(inventory);

        // Store order reservation details
        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(inventoryReservationDto.getOrderId());
        reservation.setProductId(productId);
        reservation.setQuantityReserved(reservationQuantity);
        reservation.setCreatedAt(LocalDateTime.now());
        InventoryReservationDto InventoryReservationDto = InventoryMapper.INSTANCE.toReservationDTO(inventoryReservationRepository.save(reservation));

        return InventoryReservationDto;
    }
*/
    @Transactional
    public InventoryResponseDto reserveInventory(List<InventoryReservationDto> inventoryReservationDtos) throws JsonProcessingException {
        // List to collect successful reservations
        List<InventoryReservationDto> reservedItems = new ArrayList<>();
        Long orderId = null;
        for (InventoryReservationDto inventoryReservationDto : inventoryReservationDtos) {
            Integer reservationQuantity = inventoryReservationDto.getQuantityReserved();
            Long productId = inventoryReservationDto.getProductId();
            orderId=inventoryReservationDto.getOrderId();
            // Fetch inventory for the product
            Optional<Inventory> optionalInventory = inventoryRepository.findInventoryByProductId(productId);
            Inventory inventory=new Inventory();
            if (optionalInventory.isPresent())
            {

                inventory=optionalInventory.get();

            }
            else
            {
                kafkaProducer.publishOrderDatum( new InventoryResponseDto("Inventory Reservation Failed",orderId.toString(),"Failed"));
            return    new InventoryResponseDto("Inventory Reservation Failed",orderId.toString(),"Failed");

            }

            Integer inventoryQuantity = inventory.getQuantity();

            // Check if sufficient inventory is available
            if (inventoryQuantity < reservationQuantity) {
                kafkaProducer.publishOrderDatum( new InventoryResponseDto("Inventory Reservation Failed",orderId.toString(),"Failed"));
                return new InventoryResponseDto("Inventory Reservation Failed",orderId.toString(),"Failed");
            }

            // Update inventory quantities
            inventory.setQuantity(inventoryQuantity - reservationQuantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + reservationQuantity);
            inventoryRepository.save(inventory);

            // Store order reservation details
            InventoryReservation reservation = new InventoryReservation();
            reservation.setOrderId(inventoryReservationDto.getOrderId());
            reservation.setProductId(productId);
            reservation.setQuantityReserved(reservationQuantity);
            reservation.setCreatedAt(LocalDateTime.now());

            // Save reservation and add to the response list
            InventoryReservationDto reservedDto = InventoryMapper.INSTANCE.toReservationDTO(inventoryReservationRepository.save(reservation));
            reservedItems.add(reservedDto);
        }
        InventoryResponseDto inventoryResponseDto=  new InventoryResponseDto("Inventory Successfully Reserved",orderId.toString(),"Success");
        kafkaProducer.publishOrderDatum(inventoryResponseDto);
        return inventoryResponseDto;
        // Return the list of successfully reserved items
    }

    @Transactional
    public InventoryResponseDto unreserveInventory(Long orderId) throws JsonProcessingException {
        // Step 1: Fetch reserved items for the order
        List<InventoryReservation> reservedItems = inventoryReservationRepository.findInventoryReservationByOrderId(orderId);

        if (reservedItems.isEmpty()) {
            throw new IllegalArgumentException("No reservations found for order ID: " + orderId);
        }

        // Step 2: Update inventory for each reserved item
        for (InventoryReservation reservation : reservedItems) {
            Inventory inventory = inventoryRepository.findInventoryByProductId(reservation.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + reservation.getProductId()));

            // Decrease the reserved quantity
            int updatedReservedQuantity = inventory.getReservedQuantity() - reservation.getQuantityReserved();
            int availableQuantity=inventory.getQuantity()+reservation.getQuantityReserved();
            if (updatedReservedQuantity < 0) {
                throw new IllegalStateException("Reserved quantity cannot be negative for product ID: " + reservation.getProductId());
            }

            inventory.setReservedQuantity(updatedReservedQuantity);
            inventory.setQuantity(availableQuantity);
            inventoryRepository.save(inventory);
        }

        // Step 3: Remove reservation records
        inventoryReservationRepository.deleteInventoryResevationByOrderId(orderId);
        InventoryResponseDto inventoryResponseDto=    new InventoryResponseDto("Inventory Successfully Unreserved",orderId.toString(),"UnReserved-Success");
        kafkaProducer.publishOrderDatum(inventoryResponseDto);
        return inventoryResponseDto;
    }


}
