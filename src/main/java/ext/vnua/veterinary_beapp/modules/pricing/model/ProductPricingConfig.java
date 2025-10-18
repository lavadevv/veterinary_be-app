package ext.vnua.veterinary_beapp.modules.pricing.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_product_config")
@Getter @Setter
public class ProductPricingConfig {

    @Id
    private Long productId; // 1-1 theo product

    @Column(nullable = false)
    private Long formulaId;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal batchSizeKg;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
