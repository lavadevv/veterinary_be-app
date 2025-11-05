# Product-Brand Refactoring Summary

## 🎯 Mục tiêu
Tách biệt thông tin sản phẩm core và thông tin theo brand/quy cách đóng gói để:
- **1 công thức** → **n sản phẩm**
- **1 sản phẩm** ↔ **m brands** (many-to-many)
- Mỗi brand có chi phí sản xuất, quy cách đóng gói, và giá bán riêng
- Giá bán tự động tính toán dựa trên: `(materialCost + productionUnitCost) × (1 + profit%) × (1 + VAT%)`

---

## 📦 Entities Đã Thay Đổi

### 1. **Product** (Refactored)
**Location:** `modules/product/model/Product.java`

#### Các field đã XÓA (chuyển sang ProductBrand):
- ❌ `brandName`
- ❌ `packagingSpecification`
- ❌ `registrationNumber`
- ❌ `circulationCode`
- ❌ `qualityStandard`
- ❌ `costPrice`
- ❌ `profitMarginPercentage`
- ❌ `sellingPrice`

#### Các field còn lại (Product Core):
- ✅ `productCode` - Mã sản phẩm (unique)
- ✅ `productName` - Tên sản phẩm
- ✅ `shortName` - Tên ngắn
- ✅ `productCategory` - Loại sản phẩm
- ✅ `formulationType` - Dạng bào chế
- ✅ `shelfLifeMonths` - Hạn sử dụng
- ✅ `unitOfMeasure` - Đơn vị tính
- ✅ `currentStock` - Tồn kho
- ✅ `minimumStockLevel` - Mức tồn tối thiểu
- ✅ `requiresColdStorage` - Yêu cầu bảo quản lạnh
- ✅ `specialStorageConditions` - Điều kiện bảo quản đặc biệt
- ✅ `isActive` - Trạng thái
- ✅ `notes` - Ghi chú

#### Relationship mới:
```java
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, 
           fetch = FetchType.LAZY, orphanRemoval = true)
private List<ProductBrand> productBrands;
```

---

### 2. **ProductBrand** (NEW - Bảng trung gian)
**Location:** `modules/product/model/ProductBrand.java`

#### Structure:
```java
@Entity
@Table(name = "product_brands")
public class ProductBrand extends AuditableEntity {
    
    // === RELATIONSHIPS ===
    @ManyToOne
    private Product product;
    
    @ManyToOne
    private Brand brand;
    
    @ManyToOne
    private ProductionCostSheet productionCostSheet;
    
    // === PACKAGING & SPECIFICATION ===
    private String packagingSpecification;  // "1/1", "10/1", etc.
    
    // === REGISTRATION INFO ===
    private String registrationNumber;
    private String circulationCode;
    private String qualityStandard;
    
    // === COST & PRICING ===
    private BigDecimal materialCost;              // Chi phí nguyên liệu (từ Formula)
    private BigDecimal productionUnitCost;        // Chi phí sản xuất (từ ProductionCostSheet.unitCost)
    private BigDecimal profitMarginPercentage;    // Lợi nhuận % (0-100)
    private BigDecimal vatPercentage;             // VAT % (0-100)
    private BigDecimal sellingPrice;              // Giá bán (TỰ ĐỘNG TÍNH)
    
    private Boolean isActive;
    private String notes;
}
```

#### 🔥 Auto-calculation Logic:
```java
@PrePersist
@PreUpdate
public void calculateSellingPrice() {
    BigDecimal baseCost = materialCost + productionUnitCost;
    BigDecimal costWithProfit = baseCost × (1 + profitMargin/100);
    sellingPrice = costWithProfit × (1 + vat/100);
}
```

**Ví dụ tính giá:**
- Material Cost: 10,000 VND
- Production Cost: 5,000 VND
- Profit Margin: 10.5% 
- VAT: 0%
- **→ Selling Price = (10,000 + 5,000) × 1.105 × 1.0 = 16,575 VND** ✅

---

### 3. **ProductBrandRepository** (NEW)
**Location:** `modules/product/repository/ProductBrandRepository.java`

#### Query Methods:
```java
// Tìm theo product + brand
Optional<ProductBrand> findByProductIdAndBrandId(Long productId, Long brandId);

// Danh sách theo product
List<ProductBrand> findByProductId(Long productId);

// Danh sách theo brand
List<ProductBrand> findByBrandId(Long brandId);

// Danh sách theo ProductionCostSheet
List<ProductBrand> findByProductionCostSheetId(Long sheetId);

// Kiểm tra tồn tại
boolean existsByProductIdAndBrandId(Long productId, Long brandId);

// Danh sách active
List<ProductBrand> findActiveByProductId(Long productId);
List<ProductBrand> findActiveByBrandId(Long brandId);
```

---

## 📊 DTOs Đã Tạo

### 1. **ProductBrandDto**
**Location:** `modules/product/dto/ProductBrandDto.java`

Response DTO chứa đầy đủ thông tin (include product name, brand name, cost sheet name)

### 2. **UpsertProductBrandRequest**
**Location:** `modules/product/dto/UpsertProductBrandRequest.java`

Request DTO với validation:
- `@NotNull` cho productId, brandId, profitMargin, VAT
- `@DecimalMin/@DecimalMax` cho profit (0-100), VAT (0-100)
- `@Size` cho các string fields

---

## 🔄 Workflow Sử Dụng

### Case Study: Sản phẩm Lactoc với 2 brands

#### 1. Tạo Product (Core info):
```json
{
  "productCode": "LACTOC-001",
  "productName": "Lactoc",
  "formulationType": "POWDER",
  "shelfLifeMonths": 24,
  "unitOfMeasure": "kg"
}
```

#### 2. Tạo ProductBrand #1 (Lactoc - Daeyong):
```json
{
  "productId": 1,
  "brandId": 10, // Daeyong
  "productionCostSheetId": 100,
  "packagingSpecification": "1/1",
  "materialCost": 3637,        // Từ Formula
  "productionUnitCost": 11230,  // Từ ProductionCostSheet
  "profitMarginPercentage": 10.5,
  "vatPercentage": 0,
  "registrationNumber": "REG-001-DY"
}
// → sellingPrice = (3637 + 11230) × 1.105 × 1.0 = 16,428 VND ✅
```

#### 3. Tạo ProductBrand #2 (Lactoc - Yowin):
```json
{
  "productId": 1,
  "brandId": 15, // Yowin
  "productionCostSheetId": 101,
  "packagingSpecification": "3/1",
  "materialCost": 3637,        // Same formula
  "productionUnitCost": 14634,  // Different production cost
  "profitMarginPercentage": 6,
  "vatPercentage": 0,
  "registrationNumber": "REG-001-YW"
}
// → sellingPrice = (3637 + 14634) × 1.06 × 1.0 = 19,367 VND ✅
```

---

## 🎯 Integration Points

### 1. **Với ProductFormula (Material Cost)**
```java
// Khi formula thay đổi, cập nhật materialCost trong ProductBrand
productBrand.setMaterialCost(calculatedMaterialCost);
productBrand.calculateSellingPrice(); // Auto-recalc
productBrandRepository.save(productBrand);
```

### 2. **Với ProductionCostSheet**
```java
// Helper method trong ProductBrand
productBrand.updateProductionCostFromSheet();
// → Tự động lấy unitCost từ ProductionCostSheet và recalc sellingPrice
```

### 3. **Query Examples**
```java
// Lấy tất cả brands của 1 product
List<ProductBrand> brands = productBrandRepository.findByProductId(productId);

// Tìm brand cụ thể
Optional<ProductBrand> pb = productBrandRepository
    .findByProductIdAndBrandId(productId, brandId);

// Lấy products sử dụng 1 cost sheet
List<ProductBrand> products = productBrandRepository
    .findByProductionCostSheetId(costSheetId);
```

---

## 📝 Database Schema

### Table: `product_brands`
```sql
CREATE TABLE product_brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    production_cost_sheet_id BIGINT,
    
    packaging_specification VARCHAR(300),
    registration_number VARCHAR(150),
    circulation_code VARCHAR(150),
    quality_standard TEXT,
    
    material_cost DECIMAL(18,2) DEFAULT 0,
    production_unit_cost DECIMAL(18,2) DEFAULT 0,
    profit_margin_percentage DECIMAL(5,2) DEFAULT 0,
    vat_percentage DECIMAL(5,2) DEFAULT 0,
    selling_price DECIMAL(18,2) DEFAULT 0,
    
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_product_brand UNIQUE (product_id, brand_id),
    CONSTRAINT fk_pb_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_pb_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
    CONSTRAINT fk_pb_cost_sheet FOREIGN KEY (production_cost_sheet_id) 
        REFERENCES production_cost_sheets(id),
    
    INDEX idx_pb_product (product_id),
    INDEX idx_pb_brand (brand_id),
    INDEX idx_pb_active (is_active)
);
```

---

## ✅ Validation Rules

1. **Profit Margin**: 0 ≤ value ≤ 100
2. **VAT**: 0 ≤ value ≤ 100
3. **Material Cost**: ≥ 0
4. **Production Unit Cost**: ≥ 0
5. **Unique Constraint**: Một product chỉ có 1 record với 1 brand

---

## 🚀 Next Steps (Backend)

### Cần implement:
1. ✅ **ProductBrandService** - CRUD operations
2. ✅ **ProductBrandController** - REST endpoints
3. ✅ **ProductBrandMapper** - Entity ↔ DTO conversion
4. ⚠️ **Update ProductService** - Include productBrands in response
5. ⚠️ **Integration với Formula calculation** - Auto-update materialCost
6. ⚠️ **Trigger update** khi ProductionCostSheet thay đổi

---

## 📱 Frontend Requirements

### Screens cần update:
1. **Product Form** - Remove brand-related fields
2. **Product Brand Management** - NEW screen để manage brands
3. **Pricing Calculator** - Show auto-calculated selling price
4. **Product List** - Show brands as expandable rows

### Example UI Flow:
```
Product Detail Page
├── Product Core Info (code, name, category...)
└── Brands Tab
    ├── [Daeyong - 1/1] → 16,428 VND
    ├── [Yowin - 3/1] → 19,367 VND
    └── [+ Add New Brand]
```

---

## 🎓 Key Benefits

✅ **Flexibility**: Một sản phẩm có thể có nhiều brands với giá khác nhau  
✅ **Auto-calculation**: Giá bán tự động tính, không cần user nhập  
✅ **Traceability**: Biết được giá bán đến từ đâu (material + production + profit + VAT)  
✅ **Scalability**: Dễ thêm brand mới cho product hiện có  
✅ **Data Integrity**: Unique constraint đảm bảo không duplicate product-brand  

---

## 📌 Important Notes

⚠️ **Migration Strategy**: 
- Old data trong `products` table (brandName, costPrice, etc.) cần migrate sang `product_brands`
- Hoặc giữ lại để backward compatibility, gradually migrate

⚠️ **Performance**:
- Use `@ManyToOne(fetch = FetchType.LAZY)` để tránh N+1 query
- Consider using `@EntityGraph` khi cần load brands cùng product

⚠️ **Business Logic**:
- Validate `profit_margin_percentage` và `vat_percentage` trong range [0, 100]
- Đảm bảo `materialCost` được sync với Formula calculation
- Đảm bảo `productionUnitCost` được sync với ProductionCostSheet

---

**Generated:** November 3, 2025  
**Status:** ✅ Implementation Complete - Ready for Service Layer
