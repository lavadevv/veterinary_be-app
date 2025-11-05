# Tài liệu: Refactor MaterialBatch - Hỗ trợ nhiều vật liệu trong một lô

## Tổng quan thay đổi

### Trước đây
- **MaterialBatch**: Một lô chỉ chứa một loại vật liệu
- Quan hệ: `MaterialBatch` -(Many-to-One)-> `Material`
- Một lô nhập hàng = một vật liệu

### Sau khi thay đổi
- **MaterialBatch**: Đại diện cho một lần nhập hàng (có thể chứa nhiều vật liệu)
- **MaterialBatchItem**: Đại diện cho một vật liệu cụ thể trong lô
- Quan hệ: `MaterialBatch` -(One-to-Many)-> `MaterialBatchItem` -(Many-to-One)-> `Material`
- Một lô nhập hàng = nhiều vật liệu

## Cấu trúc mới

### 1. MaterialBatch (Lô nhập hàng - Header)
Chứa thông tin chung của một lần nhập hàng:
- `batchNumber`: Mã lô nhập hàng (unique)
- `internalBatchCode`: Mã lô nội bộ
- `receivedDate`: Ngày nhập hàng
- `supplier`: Nhà cung cấp chung
- `manufacturer`: Nhà sản xuất chung (optional)
- `invoiceNumber`: Số hóa đơn
- `totalAmount`: Tổng tiền của toàn bộ lô (tự động tính từ các items)
- `location`: Vị trí kho mặc định
- `batchItems`: Danh sách các vật liệu trong lô
- `batchStatus`: Trạng thái lô (ACTIVE, COMPLETED, CANCELLED)

### 2. MaterialBatchItem (Chi tiết vật liệu trong lô)
Chứa thông tin chi tiết của từng vật liệu:
- `batch`: Lô nhập hàng cha
- `material`: Vật liệu cụ thể
- `internalItemCode`: Mã item nội bộ (unique)
- `manufacturerBatchNumber`: Mã lô NSX của vật liệu này
- `manufacturingDate`, `expiryDate`: Ngày sản xuất, hạn sử dụng
- `receivedQuantity`, `currentQuantity`: Số lượng nhập, hiện tại
- `unitPrice`, `taxPercent`: Đơn giá, thuế
- `subtotalAmount`, `taxAmount`, `totalAmount`: Thành tiền
- `testStatus`, `usageStatus`: Trạng thái kiểm nghiệm, sử dụng
- `location`, `shelfLocation`: Vị trí kho, vị trí kệ cụ thể
- `batchItemActiveIngredients`: Danh sách hoạt chất của item
- `coaNumber`, `testReportNumber`: Số COA, số báo cáo kiểm nghiệm

### 3. MaterialBatchItemActiveIngredient
Thay thế cho `MaterialBatchActiveIngredient` cũ:
- `batchItem`: MaterialBatchItem cha
- `activeIngredient`: Hoạt chất
- COA values: `coaContentValue`, `coaMinValue`, `coaMaxValue`
- Test values: `testContentValue`, `testDate`, `testMethod`

## Cách sử dụng trong Code

### Tạo mới một lô nhập hàng với nhiều vật liệu

```java
// 1. Tạo MaterialBatch (header)
MaterialBatch batch = new MaterialBatch();
batch.setBatchNumber("BATCH-2025-001");
batch.setInternalBatchCode("INT-BATCH-001");
batch.setReceivedDate(LocalDate.now());
batch.setSupplier(supplier);
batch.setInvoiceNumber("INV-2025-001");
batch.setLocation(defaultWarehouseLocation);

// 2. Tạo các MaterialBatchItem
// Item 1: Vitamin A
MaterialBatchItem item1 = new MaterialBatchItem();
item1.setMaterial(vitaminA);
item1.setInternalItemCode("INT-BATCH-001-ITEM-001");
item1.setManufacturerBatchNumber("VA-LOT-20250101");
item1.setReceivedQuantity(new BigDecimal("100.000")); // 100kg
item1.setCurrentQuantity(new BigDecimal("100.000"));
item1.setUnitPrice(new BigDecimal("500000")); // 500,000 VND/kg
item1.setTaxPercent(new BigDecimal("10")); // VAT 10%
item1.setManufacturingDate(LocalDate.of(2025, 1, 1));
item1.setExpiryDate(LocalDate.of(2027, 1, 1));
batch.addBatchItem(item1);

// Item 2: Vitamin D3
MaterialBatchItem item2 = new MaterialBatchItem();
item2.setMaterial(vitaminD3);
item2.setInternalItemCode("INT-BATCH-001-ITEM-002");
item2.setManufacturerBatchNumber("VD3-LOT-20250115");
item2.setReceivedQuantity(new BigDecimal("50.000")); // 50kg
item2.setCurrentQuantity(new BigDecimal("50.000"));
item2.setUnitPrice(new BigDecimal("800000")); // 800,000 VND/kg
item2.setTaxPercent(new BigDecimal("10"));
item2.setManufacturingDate(LocalDate.of(2025, 1, 15));
item2.setExpiryDate(LocalDate.of(2027, 1, 15));
batch.addBatchItem(item2);

// Item 3: Vitamin E
MaterialBatchItem item3 = new MaterialBatchItem();
item3.setMaterial(vitaminE);
item3.setInternalItemCode("INT-BATCH-001-ITEM-003");
item3.setManufacturerBatchNumber("VE-LOT-20250120");
item3.setReceivedQuantity(new BigDecimal("75.000")); // 75kg
item3.setCurrentQuantity(new BigDecimal("75.000"));
item3.setUnitPrice(new BigDecimal("1200000")); // 1,200,000 VND/kg
item3.setTaxPercent(new BigDecimal("10"));
item3.setManufacturingDate(LocalDate.of(2025, 1, 20));
item3.setExpiryDate(LocalDate.of(2027, 1, 20));
batch.addBatchItem(item3);

// 3. Lưu batch (cascade sẽ tự động lưu các items)
materialBatchRepository.save(batch);

// Tổng tiền sẽ tự động tính:
// Item 1: 100kg * 500,000 = 50,000,000 + 10% = 55,000,000 VND
// Item 2: 50kg * 800,000 = 40,000,000 + 10% = 44,000,000 VND
// Item 3: 75kg * 1,200,000 = 90,000,000 + 10% = 99,000,000 VND
// Total: 198,000,000 VND
```

### Thêm hoạt chất vào MaterialBatchItem

```java
MaterialBatchItem item = materialBatchItemRepository.findById(itemId).orElseThrow();

// Thêm hoạt chất với COA values
MaterialBatchItemActiveIngredient ingredient = new MaterialBatchItemActiveIngredient();
ingredient.setBatchItem(item);
ingredient.setActiveIngredient(activeIngredient);
ingredient.setCoaContentValue(new BigDecimal("99.5")); // 99.5%
ingredient.setCoaContentUnit("%");
ingredient.setCoaMinValue(new BigDecimal("98.0")); // Min 98%
ingredient.setCoaMaxValue(new BigDecimal("102.0")); // Max 102%

// Thêm kết quả test
ingredient.setTestContentValue(new BigDecimal("99.8")); // 99.8%
ingredient.setTestContentUnit("%");
ingredient.setTestDate(LocalDate.now());
ingredient.setTestMethod("HPLC");

item.getBatchItemActiveIngredients().add(ingredient);
materialBatchItemRepository.save(item);

// Kiểm tra đạt chuẩn
Boolean qualified = ingredient.isQualified(); // true (99.8% trong khoảng 98-102%)
```

### Query dữ liệu

```java
// 1. Lấy tất cả items của một lô
MaterialBatch batch = materialBatchRepository.findById(batchId).orElseThrow();
List<MaterialBatchItem> items = batch.getBatchItems();

// 2. Lấy tất cả lô chứa một vật liệu cụ thể
Material material = materialRepository.findById(materialId).orElseThrow();
List<MaterialBatchItem> batchItems = material.getBatchItems();

// 3. Tìm items theo material và location
List<MaterialBatchItem> items = materialBatchItemRepository
    .findByMaterialAndLocation(material, location);

// 4. Tìm items có số lượng khả dụng
List<MaterialBatchItem> availableItems = materialBatchItemRepository
    .findByMaterialAndAvailableQuantityGreaterThan(material, BigDecimal.ZERO);

// 5. Kiểm tra toàn bộ lô đạt chuẩn
Boolean allQualified = batch.isAllItemsQualified();
if (Boolean.FALSE.equals(allQualified)) {
    List<MaterialBatchItem> unqualified = batch.getUnqualifiedItems();
    // Xử lý các items không đạt chuẩn
}
```

## Migration Database

Để cập nhật database, chạy file migration SQL:
```bash
mysql -u username -p database_name < V1__refactor_material_batch_to_support_multiple_materials.sql
```

Hoặc nếu dùng Flyway/Liquibase, file migration sẽ tự động chạy khi khởi động ứng dụng.

## Lợi ích của cấu trúc mới

1. **Phản ánh đúng thực tế**: Một lần nhập hàng thường có nhiều loại vật liệu
2. **Quản lý tập trung**: Dễ theo dõi tổng giá trị, thông tin nhà cung cấp, hóa đơn của cả lô
3. **Linh hoạt**: Mỗi vật liệu có thể có thông tin riêng (hạn sử dụng, vị trí kệ, COA riêng)
4. **Dễ mở rộng**: Có thể thêm các tính năng như phân bổ chi phí vận chuyển, thuế chung cho lô
5. **Tối ưu storage**: Các thông tin chung (NCC, hóa đơn, ngày nhập) chỉ lưu 1 lần

## Breaking Changes

### Code cần cập nhật:

1. **Repository methods**:
   ```java
   // Cũ
   List<MaterialBatch> findByMaterial(Material material);
   
   // Mới
   List<MaterialBatchItem> findByMaterial(Material material);
   ```

2. **Service layer**:
   - Tất cả business logic liên quan đến material batch cần update để làm việc với `MaterialBatchItem`
   - Các method tính toán số lượng, giá trị cần update

3. **API/Controller**:
   - Request/Response DTOs cần update structure
   - Endpoint URLs có thể cần điều chỉnh

4. **Frontend**:
   - UI cần hiển thị danh sách items trong một batch
   - Form nhập liệu cần support multiple items

## Ví dụ DTO Structure

```java
// MaterialBatchDTO (Response)
public class MaterialBatchDTO {
    private Long id;
    private String batchNumber;
    private String internalBatchCode;
    private LocalDate receivedDate;
    private SupplierDTO supplier;
    private String invoiceNumber;
    private BigDecimal totalAmount;
    private List<MaterialBatchItemDTO> items;
    private String batchStatus;
}

// MaterialBatchItemDTO
public class MaterialBatchItemDTO {
    private Long id;
    private MaterialDTO material;
    private String internalItemCode;
    private String manufacturerBatchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal receivedQuantity;
    private BigDecimal currentQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String testStatus;
    private String usageStatus;
}

// CreateMaterialBatchRequest
public class CreateMaterialBatchRequest {
    private String batchNumber;
    private LocalDate receivedDate;
    private Long supplierId;
    private String invoiceNumber;
    private Long locationId;
    private List<CreateMaterialBatchItemRequest> items;
}

// CreateMaterialBatchItemRequest
public class CreateMaterialBatchItemRequest {
    private Long materialId;
    private String manufacturerBatchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal receivedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal taxPercent;
    private Long locationId; // Optional, override batch location
    private String shelfLocation;
}
```

## Checklist Migration

- [ ] Backup database trước khi migrate
- [ ] Chạy migration script
- [ ] Kiểm tra dữ liệu đã được migrate đúng
- [ ] Update Repository interfaces
- [ ] Update Service layer
- [ ] Update Controllers/APIs
- [ ] Update DTOs
- [ ] Update Frontend
- [ ] Test toàn bộ flow: Create, Read, Update, Delete
- [ ] Test business logic: Stock management, Pricing, Quality control
- [ ] Update documentation
- [ ] Training team về structure mới

## Support & Questions

Nếu có vấn đề trong quá trình migration, vui lòng check:
1. Log của migration script
2. Foreign key constraints
3. Data integrity của các bảng liên quan
4. Application logs khi khởi động sau migration
