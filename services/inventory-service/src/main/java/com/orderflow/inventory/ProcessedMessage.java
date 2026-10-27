package com.orderflow.inventory;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="processed_messages")
public class ProcessedMessage {
    @Id private UUID messageId;
    @Column(nullable=false) private boolean reserved;
    protected ProcessedMessage() {}
    public ProcessedMessage(UUID messageId, boolean reserved) { this.messageId=messageId; this.reserved=reserved; }
    public boolean isReserved() { return reserved; }
}
