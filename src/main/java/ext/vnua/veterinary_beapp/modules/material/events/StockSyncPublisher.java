package ext.vnua.veterinary_beapp.modules.material.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class StockSyncPublisher {
    private final ApplicationEventPublisher publisher;

    public void publishForMaterials(Collection<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) return;
        publisher.publishEvent(new MaterialStockChangedEvent(new HashSet<>(materialIds)));
    }

    public void publishForMaterial(Long materialId) {
        if (materialId == null) return;
        var set = new java.util.HashSet<Long>();
        set.add(materialId);
        publisher.publishEvent(new MaterialStockChangedEvent(set));
    }
}
