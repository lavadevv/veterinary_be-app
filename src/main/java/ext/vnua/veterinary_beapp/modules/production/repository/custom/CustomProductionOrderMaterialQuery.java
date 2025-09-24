package ext.vnua.veterinary_beapp.modules.production.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrder;
import ext.vnua.veterinary_beapp.modules.production.model.ProductionOrderMaterial;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomProductionOrderMaterialQuery {
    private CustomProductionOrderMaterialQuery(){}

    @Data @NoArgsConstructor
    public static class ProductionOrderMaterialFilterParam {
        private Long productionOrderId;
        private Long materialId;
        private Long materialBatchId;
        private String status;   // PENDING / ISSUED / USED / RETURNED (theo model hiện tại là String)
        private String keywords; // batchNumber / materialCode / materialName
        private String sortField;
        private String sortType;
    }

    public static Specification<ProductionOrderMaterial> getFilter(ProductionOrderMaterialFilterParam p) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (p.productionOrderId != null) {
                Join<ProductionOrderMaterial, ProductionOrder> oj = root.join("productionOrder");
                ps.add(cb.equal(oj.get("id"), p.productionOrderId));
            }
            if (p.materialBatchId != null) {
                Join<ProductionOrderMaterial, MaterialBatch> mbj = root.join("materialBatch");
                ps.add(cb.equal(mbj.get("id"), p.materialBatchId));
            }
            if (p.materialId != null) {
                // materialId thông qua materialBatch.material
                Join<ProductionOrderMaterial, MaterialBatch> mbj = root.join("materialBatch");
                Join<MaterialBatch, Material> mj = mbj.join("material");
                ps.add(cb.equal(mj.get("id"), p.materialId));
            }
            if (p.status != null && !p.status.isBlank()) {
                ps.add(cb.equal(root.get("status"), p.status));
            }
            if (p.keywords != null && !p.keywords.isBlank()) {
                Join<ProductionOrderMaterial, MaterialBatch> mbj = root.join("materialBatch", jakarta.persistence.criteria.JoinType.LEFT);
                var batchLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(mbj, cb, p.keywords, "batchNumber");
                Join<MaterialBatch, Material> mj = mbj.join("material", jakarta.persistence.criteria.JoinType.LEFT);
                var matCodeLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(mj, cb, p.keywords, "materialCode");
                var matNameLike = CriteriaBuilderUtil.createPredicateForSearchInsensitive(mj, cb, p.keywords, "materialName");
                ps.add(cb.or(batchLike, matCodeLike, matNameLike));
            }

            if (p.sortField != null && !p.sortField.isBlank()) {
                if (Constant.SortType.ASC.equals(p.sortType)) query.orderBy(cb.asc(root.get(p.sortField)));
                else query.orderBy(cb.desc(root.get(p.sortField)));
            } else {
                query.orderBy(cb.desc(root.get("id")));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
