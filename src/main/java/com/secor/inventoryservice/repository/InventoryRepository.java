package com.secor.inventoryservice.repository;

import com.secor.inventoryservice.entity.Inventory;
import com.secor.inventoryservice.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findInventoryByProductId(Long productId);
    Optional<Inventory> findInventoryByRequestId(String requestId);

}
