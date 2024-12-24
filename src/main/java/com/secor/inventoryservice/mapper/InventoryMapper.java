package com.secor.inventoryservice.mapper;

import com.secor.inventoryservice.dto.InventoryDto;
import com.secor.inventoryservice.dto.InventoryReservationDto;
import com.secor.inventoryservice.entity.Inventory;
import com.secor.inventoryservice.entity.InventoryReservation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryMapper INSTANCE = Mappers.getMapper(InventoryMapper.class);
    InventoryDto toDTO(Inventory entity);
    List<InventoryDto> toDTOList(List<Inventory> entity);
    Inventory toEntity(InventoryDto productDto);
    InventoryReservationDto toReservationDTO(InventoryReservation reservation);
}
