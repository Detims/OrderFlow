package com.orderflow.inventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface InventoryRepository extends JpaRepository<InventoryItem,String> {
    @Modifying @Query("update InventoryItem i set i.available = i.available - :quantity where i.sku = :sku and i.available >= :quantity")
    int reserve(@Param("sku") String sku, @Param("quantity") int quantity);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from InventoryItem i where i.sku=:sku") Optional<InventoryItem> lockSku(@Param("sku") String sku);
}
