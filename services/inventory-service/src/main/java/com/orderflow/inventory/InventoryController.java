package com.orderflow.inventory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/inventory")
public class InventoryController {
    private final InventoryRepository inventory;
    public InventoryController(InventoryRepository inventory) { this.inventory=inventory; }
    @PutMapping("/{sku}") public ItemResponse set(@PathVariable String sku,@Valid @RequestBody SetStockRequest body) {
        var item=inventory.save(new InventoryItem(sku,body.available())); return new ItemResponse(item.getSku(),item.getAvailable());
    }
    @GetMapping public List<ItemResponse> list() { return inventory.findAll().stream().map(i->new ItemResponse(i.getSku(),i.getAvailable())).toList(); }
    public record SetStockRequest(@PositiveOrZero int available) {}
    public record ItemResponse(String sku,int available) {}
}
