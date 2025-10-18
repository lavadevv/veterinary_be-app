package ext.vnua.veterinary_beapp.modules.material.listeners;

import ext.vnua.veterinary_beapp.modules.material.events.MaterialStockChangedEvent;
import ext.vnua.veterinary_beapp.modules.material.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class MaterialStockSyncListener {
    private final MaterialService materialService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChanged(MaterialStockChangedEvent ev) {
        for (Long mid : ev.materialIds()) {
            materialService.syncMaterialStock(mid);
        }
    }
}
