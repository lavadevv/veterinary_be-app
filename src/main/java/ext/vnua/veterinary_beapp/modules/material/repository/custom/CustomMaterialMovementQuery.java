// File: ext/vnua/veterinary_beapp/modules/material/repository/custom/CustomMaterialMovementQuery.java
package ext.vnua.veterinary_beapp.modules.material.repository.custom;

import ext.vnua.veterinary_beapp.common.Constant;
import ext.vnua.veterinary_beapp.common.CriteriaBuilderUtil;
import ext.vnua.veterinary_beapp.modules.material.enums.MovementType;
import ext.vnua.veterinary_beapp.modules.material.model.Location;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatch;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialMovement;
import ext.vnua.veterinary_beapp.modules.material.model.Warehouse;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomMaterialMovementQuery {

    private CustomMaterialMovementQuery() {}

    @Data
    @NoArgsConstructor
    public static class MovementFilterParam {
        /** Tìm theo từ khóa: materialName/code, batchNumber/internalBatchCode, locationCode, warehouseName */
        private String keywords;

        /** Loại dịch chuyển: RECEIVE/CONSUME/TRANSFER/RESERVE/RELEASE/ADJUST */
        private MovementType movementType;

        /** Filter theo vật liệu | batch | vị trí | kho (nguồn/đích) */
        private Long materialId;
        private Long sourceBatchId;
        private Long targetBatchId;
        private Long sourceLocationId;
        private Long targetLocationId;
        private Long sourceWarehouseId;
        private Long targetWarehouseId;

        /** Thời gian phát sinh dịch chuyển */
        private LocalDateTime fromTime;
        private LocalDateTime toTime;

        /** Biên độ số lượng */
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;

        /** Sorting */
        // movementTime | quantity | materialName | sourceWarehouseName | targetWarehouseName | sourceLocationCode | targetLocationCode
        private String sortField;
        /** ASC | DESC */
        private String sortType;
    }

    public static Specification<MaterialMovement> getFilterMovement(MovementFilterParam param) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ====== JOINs ======
            Join<MaterialMovement, MaterialBatch> srcBatchJoin  = root.join("sourceBatch", JoinType.LEFT);
            Join<MaterialMovement, MaterialBatch> tgtBatchJoin  = root.join("targetBatch", JoinType.LEFT);

            Join<MaterialMovement, Location> srcLocJoin = root.join("sourceLocation", JoinType.LEFT);
            Join<MaterialMovement, Location> tgtLocJoin = root.join("targetLocation", JoinType.LEFT);

            Join<Location, Warehouse> srcWhJoin = srcLocJoin.join("warehouse", JoinType.LEFT);
            Join<Location, Warehouse> tgtWhJoin = tgtLocJoin.join("warehouse", JoinType.LEFT);

            Join<MaterialBatch, Material> srcMatJoin = srcBatchJoin.join("material", JoinType.LEFT);
            Join<MaterialBatch, Material> tgtMatJoin = tgtBatchJoin.join("material", JoinType.LEFT);

            // ====== FETCH để tránh LazyInitializationException ở query dữ liệu (không áp COUNT) ======
            Class<?> resultType = query.getResultType();
            boolean isCountQuery = Long.class.equals(resultType) || long.class.equals(resultType);
            if (!isCountQuery) {
                root.fetch("sourceBatch", JoinType.LEFT).fetch("material", JoinType.LEFT);
                root.fetch("targetBatch", JoinType.LEFT).fetch("material", JoinType.LEFT);
                root.fetch("sourceLocation", JoinType.LEFT).fetch("warehouse", JoinType.LEFT);
                root.fetch("targetLocation", JoinType.LEFT).fetch("warehouse", JoinType.LEFT);
                query.distinct(true);
            }

            // ====== Keywords ======
            if (param.getKeywords() != null && !param.getKeywords().trim().isEmpty()) {
                String kw = param.getKeywords().trim();

                Predicate pMatSrcName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(srcMatJoin, cb, kw, "materialName");
                Predicate pMatSrcCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(srcMatJoin, cb, kw, "materialCode");

                Predicate pMatTgtName = CriteriaBuilderUtil.createPredicateForSearchInsensitive(tgtMatJoin, cb, kw, "materialName");
                Predicate pMatTgtCode = CriteriaBuilderUtil.createPredicateForSearchInsensitive(tgtMatJoin, cb, kw, "materialCode");

                Predicate pSrcBatchNo  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(srcBatchJoin, cb, kw, "batchNumber");
                Predicate pSrcBatchInt = CriteriaBuilderUtil.createPredicateForSearchInsensitive(srcBatchJoin, cb, kw, "internalBatchCode");
                Predicate pTgtBatchNo  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(tgtBatchJoin, cb, kw, "batchNumber");
                Predicate pTgtBatchInt = CriteriaBuilderUtil.createPredicateForSearchInsensitive(tgtBatchJoin, cb, kw, "internalBatchCode");

                Predicate pSrcLocCode  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(srcLocJoin, cb, kw, "locationCode");
                Predicate pTgtLocCode  = CriteriaBuilderUtil.createPredicateForSearchInsensitive(tgtLocJoin, cb, kw, "locationCode");

                Predicate pSrcWhName   = CriteriaBuilderUtil.createPredicateForSearchInsensitive(srcWhJoin, cb, kw, "warehouseName");
                Predicate pTgtWhName   = CriteriaBuilderUtil.createPredicateForSearchInsensitive(tgtWhJoin, cb, kw, "warehouseName");

                predicates.add(cb.or(
                        pMatSrcName, pMatSrcCode, pMatTgtName, pMatTgtCode,
                        pSrcBatchNo, pSrcBatchInt, pTgtBatchNo, pTgtBatchInt,
                        pSrcLocCode, pTgtLocCode, pSrcWhName, pTgtWhName
                ));
            }

            // ====== Loại dịch chuyển ======
            if (param.getMovementType() != null) {
                predicates.add(cb.equal(root.get("movementType"), param.getMovementType()));
            }

            // ====== By material (match ở source hoặc target) ======
            if (param.getMaterialId() != null) {
                predicates.add(cb.or(
                        cb.equal(srcMatJoin.get("id"), param.getMaterialId()),
                        cb.equal(tgtMatJoin.get("id"), param.getMaterialId())
                ));
            }

            // ====== By batch/location/warehouse ======
            if (param.getSourceBatchId() != null) {
                predicates.add(cb.equal(srcBatchJoin.get("id"), param.getSourceBatchId()));
            }
            if (param.getTargetBatchId() != null) {
                predicates.add(cb.equal(tgtBatchJoin.get("id"), param.getTargetBatchId()));
            }
            if (param.getSourceLocationId() != null) {
                predicates.add(cb.equal(srcLocJoin.get("id"), param.getSourceLocationId()));
            }
            if (param.getTargetLocationId() != null) {
                predicates.add(cb.equal(tgtLocJoin.get("id"), param.getTargetLocationId()));
            }
            if (param.getSourceWarehouseId() != null) {
                predicates.add(cb.equal(srcWhJoin.get("id"), param.getSourceWarehouseId()));
            }
            if (param.getTargetWarehouseId() != null) {
                predicates.add(cb.equal(tgtWhJoin.get("id"), param.getTargetWarehouseId()));
            }

            // ====== Time range ======
            if (param.getFromTime() != null && param.getToTime() != null) {
                predicates.add(cb.between(root.get("movementTime"), param.getFromTime(), param.getToTime()));
            } else if (param.getFromTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("movementTime"), param.getFromTime()));
            } else if (param.getToTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("movementTime"), param.getToTime()));
            }

            // ====== Quantity range ======
            if (param.getMinQuantity() != null && param.getMaxQuantity() != null) {
                predicates.add(cb.between(root.get("quantity"), param.getMinQuantity(), param.getMaxQuantity()));
            } else if (param.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), param.getMinQuantity()));
            } else if (param.getMaxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), param.getMaxQuantity()));
            }

            // ====== Sorting ======
            String sortField = param.getSortField();
            String sortType  = param.getSortType();
            boolean asc = Constant.SortType.ASC.equalsIgnoreCase(sortType);

            if (sortField != null && !sortField.isBlank()) {
                switch (sortField) {
                    case "movementTime" -> query.orderBy(asc ? cb.asc(root.get("movementTime")) : cb.desc(root.get("movementTime")));
                    case "quantity"     -> query.orderBy(asc ? cb.asc(root.get("quantity")) : cb.desc(root.get("quantity")));
                    case "materialName" -> query.orderBy(asc ? cb.asc(srcMatJoin.get("materialName")) : cb.desc(srcMatJoin.get("materialName")));
                    case "sourceWarehouseName" -> query.orderBy(asc ? cb.asc(srcWhJoin.get("warehouseName")) : cb.desc(srcWhJoin.get("warehouseName")));
                    case "targetWarehouseName" -> query.orderBy(asc ? cb.asc(tgtWhJoin.get("warehouseName")) : cb.desc(tgtWhJoin.get("warehouseName")));
                    case "sourceLocationCode"  -> query.orderBy(asc ? cb.asc(srcLocJoin.get("locationCode")) : cb.desc(srcLocJoin.get("locationCode")));
                    case "targetLocationCode"  -> query.orderBy(asc ? cb.asc(tgtLocJoin.get("locationCode")) : cb.desc(tgtLocJoin.get("locationCode")));
                    default -> query.orderBy(cb.desc(root.get("movementTime"))); // fallback
                }
            } else {
                query.orderBy(cb.desc(root.get("movementTime"))); // mặc định mới nhất trước
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
