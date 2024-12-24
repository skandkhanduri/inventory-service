package com.secor.inventoryservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.secor.inventoryservice.dto.InventoryDto;
import com.secor.inventoryservice.dto.InventoryReservationDto;
import com.secor.inventoryservice.dto.InventoryResponseDto;
import com.secor.inventoryservice.entity.Inventory;
import com.secor.inventoryservice.service.InventoryServices;
import com.secor.inventoryservice.mapper.InventoryMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/Inventory/v1")
public class InventoryController {


    private InventoryServices InventoryServices;
    InventoryController(InventoryServices InventoryServices)
    {
        this.InventoryServices=InventoryServices;
    }
    @GetMapping("/all")
    public ResponseEntity<Object> getAllInventory()
    {
  List<Inventory> customer=InventoryServices.getAllInventory();
        List<InventoryDto> responseDto= InventoryMapper.INSTANCE.toDTOList(customer);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/getInventory")
    public ResponseEntity<Object> getInventoryById(@RequestParam("productId") Long productId
                                             ) throws JsonProcessingException {
        System.out.println("Inside the controller");
      Inventory inventory=  InventoryServices.getInventoryById(productId).get();
      InventoryDto responseDto= InventoryMapper.INSTANCE.toDTO(inventory);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @PostMapping("/createInventory")
    public ResponseEntity<Object> createInventory(@RequestBody Inventory inventory) {
        InventoryResponseDto inventoryResponseDto = InventoryServices.createInventory(inventory);

        return new ResponseEntity<>(inventoryResponseDto, HttpStatus.OK);
    }

    @PutMapping("/updateInventory")
    public ResponseEntity<Object> updateInventory( @RequestBody Inventory inventory) {
        Inventory updatedPayment = InventoryServices.updateInventory(inventory);
        InventoryDto responseDto= InventoryMapper.INSTANCE.toDTO(updatedPayment);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/deleteInventory")
    public ResponseEntity<Void> deletePayment(@RequestParam("inventoryId") Long inventoryId) {
        InventoryServices.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getInventoryStatus")
    public ResponseEntity<Object> getInventoryStatus(@RequestParam("requestId") String RequestId)
    {
        InventoryResponseDto inventoryResponseDto=   InventoryServices.getInventoryStatus(RequestId);
        return new ResponseEntity<>(inventoryResponseDto, HttpStatus.OK);
    }

    @GetMapping("/checkInventoryQuantity")
    public ResponseEntity<Object> checkInventoryQuantity(@RequestParam("productId") Long productId,@RequestParam("quantity") Integer quantity)
    {
        InventoryResponseDto inventoryResponseDto=   InventoryServices.checkInventoryQuantity(productId,quantity);
        return new ResponseEntity<>(inventoryResponseDto, HttpStatus.OK);
    }

    @PostMapping("/reserveInventory")
    public ResponseEntity<Object> reserveInventory(@RequestBody List<InventoryReservationDto> reservationDto) throws JsonProcessingException  {
        InventoryResponseDto inventoryResponseDto = InventoryServices.reserveInventory(reservationDto);

        return new ResponseEntity<>(inventoryResponseDto, HttpStatus.OK);
    }

    @PutMapping("/unReserveInventory")
    public ResponseEntity<Object> unReserveInventory(@RequestParam("orderID") Long orderId) throws JsonProcessingException {
        InventoryResponseDto inventoryResponseDto = InventoryServices.unreserveInventory(orderId);

        return new ResponseEntity<>(inventoryResponseDto, HttpStatus.OK);
    }

}
