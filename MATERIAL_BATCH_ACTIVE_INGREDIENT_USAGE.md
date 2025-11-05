# Material Batch Active Ingredient Usage Guide

## 📋 Tổng quan thiết kế

### Cấu trúc quan hệ:
```
Material (Nguyên liệu)
    ├── Material.activeIngredients (Danh sách hoạt chất của nguyên liệu)
    │       └── MaterialActiveIngredient
    │               ├── activeIngredient (Hoạt chất)
    │               ├── contentValue (Hàm lượng tiêu chuẩn)
    │               └── contentUnit (Đơn vị)
    │
    └── Material.batches (Các lô nguyên liệu)
            └── MaterialBatch
                    └── batchActiveIngredients (Hàm lượng thực tế của từng hoạt chất trong lô)
                            └── MaterialBatchActiveIngredient
                                    ├── activeIngredient (Hoạt chất)
                                    ├── coaContent (Hàm lượng theo COA)
                                    ├── coaContentUnit (Đơn vị COA)
                                    ├── actualContent (Hàm lượng KQPT)
                                    ├── actualContentUnit (Đơn vị KQPT)
                                    └── isQualified() (Kiểm tra đạt/không đạt)
```

## 🎯 Ví dụ cụ thể

### Ví dụ 1: Vitamin A có nhiều dạng
```
Material: VITAMIN_A_POWDER
    ├── ActiveIngredient 1: Retinyl Acetate (500,000 IU/g)
    ├── ActiveIngredient 2: Retinyl Palmitate (1,700,000 IU/g)
    └── ActiveIngredient 3: Beta-carotene (30%)

MaterialBatch: LOT-VA-2024-001
    ├── BatchActiveIngredient 1:
    │       ├── ActiveIngredient: Retinyl Acetate
    │       ├── COA: 500,000 IU/g
    │       ├── KQPT: 485,000 IU/g
    │       ├── Ratio: 97% ✅ Đạt (90-110%)
    │
    ├── BatchActiveIngredient 2:
    │       ├── ActiveIngredient: Retinyl Palmitate
    │       ├── COA: 1,700,000 IU/g
    │       ├── KQPT: 1,650,000 IU/g
    │       ├── Ratio: 97% ✅ Đạt
    │
    └── BatchActiveIngredient 3:
            ├── ActiveIngredient: Beta-carotene
            ├── COA: 30%
            ├── KQPT: 28%
            ├── Ratio: 93.3% ✅ Đạt

KẾT LUẬN: Lô này ĐẠT (tất cả hoạt chất đều đạt)
```

### Ví dụ 2: Kháng sinh có nhiều hoạt chất
```
Material: AMOXICILLIN_TRIHYDRATE
    ├── ActiveIngredient 1: Amoxicillin (80%)
    └── ActiveIngredient 2: Clavulanic Acid (20%)

MaterialBatch: LOT-AMX-2024-002
    ├── BatchActiveIngredient 1:
    │       ├── ActiveIngredient: Amoxicillin
    │       ├── COA: 80%
    │       ├── KQPT: 72%
    │       ├── Ratio: 90% ✅ Đạt (giới hạn dưới)
    │
    └── BatchActiveIngredient 2:
            ├── ActiveIngredient: Clavulanic Acid
            ├── COA: 20%
            ├── KQPT: 16%
            ├── Ratio: 80% ❌ KHÔNG ĐẠT (<90%)

KẾT LUẬN: Lô này KHÔNG ĐẠT (Clavulanic Acid không đạt)
```

## 💻 Code Example - Tạo lô mới với nhiều hoạt chất

```java
// 1. Tạo MaterialBatch
MaterialBatch batch = new MaterialBatch();
batch.setBatchNumber("LOT-VA-2024-001");
batch.setMaterial(vitaminAMaterial);
batch.setReceivedQuantity(new BigDecimal("100"));
batch.setCurrentQuantity(new BigDecimal("100"));
batch.setReceivedDate(LocalDate.now());
batch.setShelfLocation("A-01-05");
batch.setImagePath("/uploads/batches/LOT-VA-2024-001.jpg");

// 2. Thêm hoạt chất 1: Retinyl Acetate
MaterialBatchActiveIngredient ingredient1 = new MaterialBatchActiveIngredient();
ingredient1.setBatch(batch);
ingredient1.setActiveIngredient(retinylAcetate);
ingredient1.setCoaContent(new BigDecimal("500000"));
ingredient1.setCoaContentUnit("IU/g");
ingredient1.setActualContent(new BigDecimal("485000"));
ingredient1.setActualContentUnit("IU/g");

// 3. Thêm hoạt chất 2: Retinyl Palmitate
MaterialBatchActiveIngredient ingredient2 = new MaterialBatchActiveIngredient();
ingredient2.setBatch(batch);
ingredient2.setActiveIngredient(retinylPalmitate);
ingredient2.setCoaContent(new BigDecimal("1700000"));
ingredient2.setCoaContentUnit("IU/g");
ingredient2.setActualContent(new BigDecimal("1650000"));
ingredient2.setActualContentUnit("IU/g");

// 4. Thêm hoạt chất 3: Beta-carotene
MaterialBatchActiveIngredient ingredient3 = new MaterialBatchActiveIngredient();
ingredient3.setBatch(batch);
ingredient3.setActiveIngredient(betaCarotene);
ingredient3.setCoaContent(new BigDecimal("30"));
ingredient3.setCoaContentUnit("%");
ingredient3.setActualContent(new BigDecimal("28"));
ingredient3.setActualContentUnit("%");

// 5. Gán vào batch
batch.getBatchActiveIngredients().add(ingredient1);
batch.getBatchActiveIngredients().add(ingredient2);
batch.getBatchActiveIngredients().add(ingredient3);

// 6. Lưu
materialBatchRepository.save(batch);

// 7. Kiểm tra
System.out.println("Trạng thái: " + batch.getQualificationStatus()); // "Đạt"
System.out.println("Hoạt chất không đạt: " + batch.getUnqualifiedIngredients()); // []
```

## 📊 Hiển thị dữ liệu trên UI

### Bảng danh sách lô nguyên liệu:

| Mã nguyên liệu | Tên nguyên liệu | Tên Quốc tế | Hoạt chất | Hàm lượng COA | Hàm lượng KQPT | Tỷ lệ | Đơn vị | NCC | NSX | Trạng thái | Vị trí kho | Vị trí kệ | COA | Ảnh |
|----------------|----------------|-------------|-----------|---------------|----------------|-------|--------|-----|-----|------------|------------|-----------|-----|-----|
| VA-001 | Vitamin A Powder | Retinol | **Retinyl Acetate** | 500,000 | 485,000 | 97% | IU/g | ABC | XYZ | ✅ Đạt | KHO-A | A-01-05 | [📄] | [🖼️] |
| | | | **Retinyl Palmitate** | 1,700,000 | 1,650,000 | 97% | IU/g | | | | | | | |
| | | | **Beta-carotene** | 30 | 28 | 93% | % | | | | | | | |

### DTO để hiển thị:

```java
public class MaterialBatchDisplayDTO {
    private String materialCode;
    private String materialName;
    private String internationalName;
    private String supplierName;
    private String manufacturerName;
    private String locationCode;
    private String shelfLocation;
    private String coaFilePath;
    private String imagePath;
    private String overallQualificationStatus; // "Đạt" / "Không đạt"
    
    // Danh sách hoạt chất
    private List<ActiveIngredientInfo> activeIngredients;
    
    @Data
    public static class ActiveIngredientInfo {
        private String ingredientName;
        private BigDecimal coaContent;
        private String coaUnit;
        private BigDecimal actualContent;
        private String actualUnit;
        private BigDecimal ratio; // %
        private String qualificationStatus; // "Đạt" / "Không đạt"
    }
}
```

## 🔍 Query với Entity Graph

```java
@Repository
public interface MaterialBatchRepository extends JpaRepository<MaterialBatch, Long> {
    
    @EntityGraph(value = MaterialBatch.ENTITY_GRAPH_WITH_DETAILS)
    List<MaterialBatch> findAllBy();
    
    @EntityGraph(value = MaterialBatch.ENTITY_GRAPH_WITH_DETAILS)
    Optional<MaterialBatch> findById(Long id);
}
```

## ✅ Lợi ích của thiết kế này

1. **Linh hoạt**: Mỗi lô có thể có số lượng hoạt chất khác nhau
2. **Chính xác**: Hàm lượng COA và KQPT riêng cho từng hoạt chất
3. **Dễ query**: Có thể tìm lô theo hoạt chất cụ thể
4. **Dễ mở rộng**: Thêm hoạt chất mới không cần sửa schema
5. **Tự động tính toán**: Logic kiểm nghiệm (90-110%) được tích hợp sẵn
6. **Đơn vị linh hoạt**: Mỗi hoạt chất có thể có đơn vị khác nhau (%, IU/g, mg/g...)

## 🎨 Tips khi sử dụng

1. **Khi tạo lô mới**: Copy danh sách hoạt chất từ Material, sau đó điền COA và KQPT
2. **Khi hiển thị**: Dùng Entity Graph để tránh N+1 query
3. **Khi kiểm tra đạt/không đạt**: Gọi `batch.getQualificationStatus()`
4. **Khi tìm lô có vấn đề**: Dùng `batch.getUnqualifiedIngredients()`
5. **Khi export báo cáo**: Có thể loop qua `batch.getBatchActiveIngredients()`

## 🚀 Migration SQL (nếu cần)

```sql
-- Tạo bảng mới
CREATE TABLE material_batch_active_ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    active_ingredient_id BIGINT NOT NULL,
    coa_content DECIMAL(18, 6),
    coa_content_unit VARCHAR(30),
    actual_content DECIMAL(18, 6),
    actual_content_unit VARCHAR(30),
    notes TEXT,
    created_at DATETIME,
    created_by VARCHAR(100),
    updated_at DATETIME,
    updated_by VARCHAR(100),
    CONSTRAINT fk_mbai_batch FOREIGN KEY (batch_id) REFERENCES material_batches(id),
    CONSTRAINT fk_mbai_active_ingredient FOREIGN KEY (active_ingredient_id) REFERENCES active_ingredients(id),
    CONSTRAINT uk_batch_active_ingredient UNIQUE (batch_id, active_ingredient_id)
);

-- Xóa các cột cũ (nếu đã tồn tại)
ALTER TABLE material_batches 
    DROP COLUMN IF EXISTS coa_active_content,
    DROP COLUMN IF EXISTS coa_content_unit,
    DROP COLUMN IF EXISTS actual_active_content,
    DROP COLUMN IF EXISTS actual_content_unit;
```
