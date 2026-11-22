package com.hotelbooking.inventory.application;

import com.hotelbooking.property.domain.PropertyOnboardedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Opens bookable nights for each room type of a newly onboarded property. */
@Component
class InventoryProvisioner {

    private final InventoryService inventory;

    InventoryProvisioner(InventoryService inventory) {
        this.inventory = inventory;
    }

    @EventListener
    void onPropertyOnboarded(PropertyOnboardedEvent event) {
        event.property().roomTypes().forEach(roomType -> inventory.openInventory(roomType.id(), roomType.totalRooms()));
    }
}
