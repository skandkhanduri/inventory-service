package com.secor.inventoryservice.mapper;

import com.secor.inventoryservice.dto.InventoryDto;
import com.secor.inventoryservice.dto.InventoryReservationDto;
import com.secor.inventoryservice.entity.Inventory;
import com.secor.inventoryservice.entity.InventoryReservation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-24T00:12:46+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class InventoryMapperImpl implements InventoryMapper {

    @Override
    public InventoryDto toDTO(Inventory entity) {
        if ( entity == null ) {
            return null;
        }

        InventoryDto inventoryDto = new InventoryDto();

        inventoryDto.setInventoryItemId( entity.getInventoryItemId() );
        inventoryDto.setProductId( entity.getProductId() );
        inventoryDto.setQuantity( entity.getQuantity() );
        inventoryDto.setCreatedAt( entity.getCreatedAt() );
        inventoryDto.setUpdatedAt( entity.getUpdatedAt() );

        return inventoryDto;
    }

    @Override
    public List<InventoryDto> toDTOList(List<Inventory> entity) {
        if ( entity == null ) {
            return null;
        }

        List<InventoryDto> list = new ArrayList<InventoryDto>( entity.size() );
        for ( Inventory inventory : entity ) {
            list.add( toDTO( inventory ) );
        }

        return list;
    }

    @Override
    public Inventory toEntity(InventoryDto productDto) {
        if ( productDto == null ) {
            return null;
        }

        Inventory inventory = new Inventory();

        inventory.setInventoryItemId( productDto.getInventoryItemId() );
        inventory.setProductId( productDto.getProductId() );
        inventory.setQuantity( productDto.getQuantity() );
        inventory.setCreatedAt( productDto.getCreatedAt() );
        inventory.setUpdatedAt( productDto.getUpdatedAt() );

        return inventory;
    }

    @Override
    public InventoryReservationDto toReservationDTO(InventoryReservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        InventoryReservationDto inventoryReservationDto = new InventoryReservationDto();

        inventoryReservationDto.setOrderId( reservation.getOrderId() );
        inventoryReservationDto.setProductId( reservation.getProductId() );
        inventoryReservationDto.setQuantityReserved( reservation.getQuantityReserved() );

        return inventoryReservationDto;
    }
}
