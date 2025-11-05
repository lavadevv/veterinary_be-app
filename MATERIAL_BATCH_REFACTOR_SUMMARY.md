# Tóm tắt thay đổi: MaterialBatch Refactoring

## 📋 Tổng quan

Đã refactor `MaterialBatch` từ model **một lô - một vật liệu** sang **một lô - nhiều vật liệu** để phù hợp với thực tế nghiệp vụ.

## 🆕 Các file mới được tạo

### 1. Entity Classes
- ✅ `MaterialBatchItem.java` - Chi tiết vật liệu trong lô
- ✅ `MaterialBatchItemActiveIngredient.java` - Hoạt chất của từng item

### 2. Repository Interfaces
- ✅ `MaterialBatchItemRepository.java` - Repository cho MaterialBatchItem (với nhiều query methods hữu ích)
- ✅ `MaterialBatchItemActiveIngredientRepository.java` - Repository cho hoạt chất

### 3. Database Migration
- ✅ `V1__refactor_material_batch_to_support_multiple_materials.sql` - Script migration database

### 4. Documentation
- ✅ `MATERIAL_BATCH_REFACTOR_GUIDE.md` - Hướng dẫn chi tiết cách sử dụng

## 🔄 Các file đã cập nhật

### 1. MaterialBatch.java
**Thay đổi chính:**
- ❌ Xóa: `material` field (ManyToOne relationship)
- ❌ Xóa: Các field về số lượng, giá, thuế (moved to MaterialBatchItem)
- ❌ Xóa: `batchActiveIngredients` (replaced by MaterialBatchItemActiveIngredient)
- ✅ Thêm: `batchItems` (OneToMany relationship với MaterialBatchItem)
- ✅ Thêm: `batchStatus` field
- ✅ Cập nhật: Logic tính toán `totalAmount` từ các items
- ✅ Thêm: Business methods (`addBatchItem`, `removeBatchItem`, `isAllItemsQualified`, etc.)
- ✅ Cập nhật: `toString()` method để hiển thị thông tin lô với danh sách items

**Bây giờ MaterialBatch là:**
- Header/container cho một lần nhập hàng
- Chứa thông tin chung: NCC, hóa đơn, ngày nhập
- Quản lý nhiều MaterialBatchItem

### 2. Material.java
**Thay đổi:**
- ❌ Xóa: `List<MaterialBatch> batches`
- ✅ Thêm: `List<MaterialBatchItem> batchItems`
- Lý do: Material giờ liên kết trực tiếp với MaterialBatchItem, không phải MaterialBatch

## 📊 Cấu trúc database mới

```
MaterialBatch (header/lô nhập hàng)
    │
    ├─ batchNumber (unique)
    ├─ receivedDate
    ├─ supplier
    ├─ invoiceNumber
    ├─ totalAmount (calculated)
    │
    └─► MaterialBatchItem (nhiều items)
            │
            ├─ material
            ├─ receivedQuantity
            ├─ unitPrice
            ├─ totalAmount
            │
            └─► MaterialBatchItemActiveIngredient (nhiều hoạt chất)
                    │
                    ├─ activeIngredient
                    ├─ coaContentValue (COA)
                    └─ testContentValue (KQPT)
```

## 🎯 Use Cases được hỗ trợ

### Trước đây (không hỗ trợ):
- ❌ Một lô chỉ chứa 1 loại vật liệu
- ❌ Nếu nhập 3 vật liệu cùng lúc → phải tạo 3 MaterialBatch riêng
- ❌ Khó quản lý thông tin NCC, hóa đơn chung

### Bây giờ (được hỗ trợ):
- ✅ Một lô chứa nhiều vật liệu khác nhau
- ✅ Nhập 10 vật liệu → 1 MaterialBatch + 10 MaterialBatchItem
- ✅ Thông tin chung (NCC, hóa đơn) lưu 1 lần ở MaterialBatch
- ✅ Thông tin riêng (số lượng, giá, HSD) lưu ở từng MaterialBatchItem
- ✅ Mỗi item có thể có vị trí kho riêng
- ✅ Mỗi item có thể có COA/KQPT riêng

## 📝 Ví dụ thực tế

### Scenario: Nhập hàng từ NCC Pharmaco

**Hóa đơn INV-2025-001, ngày 01/11/2025:**
- Vitamin A: 100kg @ 500,000 VND/kg
- Vitamin D3: 50kg @ 800,000 VND/kg
- Vitamin E: 75kg @ 1,200,000 VND/kg

**Trước:**
- Tạo 3 MaterialBatch riêng
- Lặp lại thông tin NCC, hóa đơn 3 lần
- Khó tính tổng giá trị lô

**Bây giờ:**
- 1 MaterialBatch (INV-2025-001, Pharmaco, 01/11/2025)
- 3 MaterialBatchItem (Vitamin A, D3, E)
- Tổng giá trị tự động = 198,000,000 VND

## 🔧 Cần làm gì tiếp theo?

### 1. Database Migration
```bash
# Backup database
mysqldump -u user -p database > backup_before_migration.sql

# Run migration
mysql -u user -p database < V1__refactor_material_batch_to_support_multiple_materials.sql
```

### 2. Code Update Checklist

#### Backend:
- [ ] Cập nhật `MaterialBatchRepository` (nếu cần thay đổi)
- [ ] Tạo/Cập nhật `MaterialBatchService`
- [ ] Tạo/Cập nhật `MaterialBatchItemService`
- [ ] Cập nhật DTOs:
  - [ ] `MaterialBatchDTO` - thêm `List<MaterialBatchItemDTO> items`
  - [ ] `MaterialBatchItemDTO` - tạo mới
  - [ ] `CreateMaterialBatchRequest` - thêm `List<CreateMaterialBatchItemRequest> items`
  - [ ] `CreateMaterialBatchItemRequest` - tạo mới
- [ ] Cập nhật Controllers:
  - [ ] `MaterialBatchController` - update endpoints
  - [ ] Thêm endpoints cho MaterialBatchItem nếu cần
- [ ] Cập nhật business logic:
  - [ ] Stock management
  - [ ] Allocation logic (FIFO/FEFO)
  - [ ] Pricing calculations
  - [ ] Quality control

#### Frontend:
- [ ] Cập nhật UI nhập lô mới:
  - [ ] Form header (thông tin chung lô)
  - [ ] Table/List để nhập nhiều items
  - [ ] Add/Remove item functionality
- [ ] Cập nhật UI xem chi tiết lô:
  - [ ] Hiển thị thông tin header
  - [ ] Hiển thị danh sách items dạng table
  - [ ] Chi tiết từng item (expandable)
- [ ] Cập nhật UI danh sách lô:
  - [ ] Hiển thị tổng số items
  - [ ] Hiển thị tổng giá trị
- [ ] Cập nhật API calls để phù hợp với structure mới

### 3. Testing
- [ ] Unit tests cho entities
- [ ] Unit tests cho repositories
- [ ] Unit tests cho services
- [ ] Integration tests
- [ ] E2E tests
- [ ] Performance tests (với nhiều items)

### 4. Data Verification
- [ ] Kiểm tra dữ liệu đã migrate đúng
- [ ] Verify foreign keys
- [ ] Verify calculations (totalAmount)
- [ ] Test với dữ liệu thực

## ⚠️ Breaking Changes

### API Endpoints (potential changes):
```
# Có thể cần update
POST /api/materials/batches          → body structure changed
GET  /api/materials/batches/{id}     → response structure changed
PUT  /api/materials/batches/{id}     → body structure changed

# Có thể cần thêm mới
GET    /api/materials/batches/{id}/items
POST   /api/materials/batches/{id}/items
PUT    /api/materials/batches/{id}/items/{itemId}
DELETE /api/materials/batches/{id}/items/{itemId}
```

### Service Methods (potential changes):
```java
// Cũ
MaterialBatch createBatch(CreateBatchRequest request);

// Mới
MaterialBatch createBatch(CreateBatchWithItemsRequest request);
```

## 📚 Tài liệu tham khảo

1. **Chi tiết implementation**: `MATERIAL_BATCH_REFACTOR_GUIDE.md`
2. **Migration script**: `V1__refactor_material_batch_to_support_multiple_materials.sql`
3. **Entity classes**: 
   - `MaterialBatch.java`
   - `MaterialBatchItem.java`
   - `MaterialBatchItemActiveIngredient.java`
4. **Repositories**:
   - `MaterialBatchItemRepository.java`
   - `MaterialBatchItemActiveIngredientRepository.java`

## 💡 Tips

1. **Khi tạo lô mới**: Luôn tạo ít nhất 1 MaterialBatchItem
2. **Khi xóa lô**: Cascade delete sẽ tự động xóa các items
3. **Khi tính tổng tiền**: `@PrePersist` và `@PreUpdate` tự động tính
4. **Khi query**: Sử dụng entity graph để tối ưu performance
5. **FIFO/FEFO**: Đã có sẵn query methods trong repository

## 🤝 Support

Nếu cần hỗ trợ trong quá trình implementation:
1. Đọc kỹ `MATERIAL_BATCH_REFACTOR_GUIDE.md`
2. Xem ví dụ code trong guide
3. Test từng bước một
4. Backup database trước khi migrate!
