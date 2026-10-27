package com.orderflow.inventory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service
public class ReservationService {
    private final InventoryRepository inventory; private final ProcessedMessageRepository processed;
    public ReservationService(InventoryRepository inventory,ProcessedMessageRepository processed){this.inventory=inventory;this.processed=processed;}
    @Transactional
    public boolean reserve(UUID messageId,String sku,int quantity) {
        var prior=processed.findById(messageId);
        if (prior.isPresent()) return prior.get().isReserved();
        // The atomic conditional update prevents concurrent orders from reserving below zero.
        boolean reserved=inventory.reserve(sku,quantity)==1;
        // The message ID primary key makes concurrent duplicates roll back and retry safely.
        processed.saveAndFlush(new ProcessedMessage(messageId,reserved));
        return reserved;
    }
}
