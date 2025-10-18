package ext.vnua.veterinary_beapp.modules.material.events;

import java.util.Set;

public record MaterialStockChangedEvent(Set<Long> materialIds) { }