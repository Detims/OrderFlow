package com.orderflow.inventory;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReservationServiceTest {
    @Test void recordsAndReturnsReservationResult() {
        var inventory=mock(InventoryRepository.class);
        var processed=mock(ProcessedMessageRepository.class);
        when(processed.findById(any())).thenReturn(Optional.empty());
        when(inventory.reserve("SKU-1",2)).thenReturn(1);
        var service=new ReservationService(inventory,processed);

        assertThat(service.reserve(UUID.randomUUID(),"SKU-1",2)).isTrue();
        verify(processed).saveAndFlush(argThat(ProcessedMessage::isReserved));
    }
    @Test void repeatsTheRecordedOutcomeForDuplicateMessages() {
        var inventory=mock(InventoryRepository.class);
        var processed=mock(ProcessedMessageRepository.class);
        var id=UUID.randomUUID();
        when(processed.findById(id)).thenReturn(Optional.of(new ProcessedMessage(id,false)));
        var service=new ReservationService(inventory,processed);

        assertThat(service.reserve(id,"SKU-1",2)).isFalse();
        verifyNoInteractions(inventory);
    }
}
