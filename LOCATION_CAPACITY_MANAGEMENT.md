# Location Capacity Management System

## Tổng quan

Hệ thống quản lý sức chứa vị trí kho tự động, cập nhật real-time khi tạo, cập nhật, di chuyển hoặc xóa lô vật liệu.

## Các thành phần chính

### 1. LocationCapacityService

Service chịu trách nhiệm quản lý sức chứa của vị trí kho.

**Các phương thức:**

- `addBatchToLocation(Long locationId, BigDecimal quantity)`: Thêm lô vào vị trí, tăng capacity
- `removeBatchFromLocation(Long locationId, BigDecimal quantity)`: Bớt lô khỏi vị trí, giảm capacity
- `moveBatch(Long fromLocationId, Long toLocationId, BigDecimal quantity)`: Di chuyển lô giữa các vị trí
- `updateBatchQuantity(Long locationId, BigDecimal oldQuantity, BigDecimal newQuantity)`: Cập nhật khi số lượng lô thay đổi
- `recalculateLocationCapacity(Long locationId)`: Tính lại toàn bộ capacity từ các lô hiện có
- `hasAvailableCapacity(Long locationId, BigDecimal quantity)`: Kiểm tra còn đủ chỗ không
- `getAvailableCapacity(Long locationId)`: Lấy sức chứa còn trống
- `getOccupancyPercentage(Long locationId)`: Lấy tỷ lệ lấp đầy (%)

### 2. MaterialBatchServiceImpl

Service đã được tích hợp LocationCapacityService để tự động cập nhật capacity.

**Các thao tác tự động cập nhật capacity:**

#### Tạo lô mới
```java
@Override
@Transactional
public MaterialBatchDto createMaterialBatch(CreateMaterialBatchRequest request) {
    // ... validate và tạo lô ...
    
    // Tự động cập nhật capacity khi tạo lô
    if (location != null) {
        locationCapacityService.addBatchToLocation(
                location.getId(), 
                savedBatch.getCurrentQuantity()
        );
    }
}
```

#### Cập nhật lô
```java
@Override
@Transactional
public MaterialBatchDto updateMaterialBatch(UpdateMaterialBatchRequest request) {
    // ... validate ...
    
    // Xử lý thay đổi vị trí hoặc số lượng
    if (oldLocationId != null && !oldLocationId.equals(newLocationId)) {
        // Di chuyển giữa các vị trí
        locationCapacityService.moveBatch(oldLocationId, newLocationId, quantity);
    } else if (newLocationId != null) {
        // Cùng vị trí, chỉ thay đổi số lượng
        locationCapacityService.updateBatchQuantity(
                newLocationId, 
                oldQuantity, 
                newQuantity
        );
    }
}
```

#### Xóa lô
```java
@Override
public void deleteMaterialBatch(Long id) {
    // ... validate ...
    
    // Giảm capacity khi xóa lô
    if (materialBatch.getLocation() != null) {
        locationCapacityService.removeBatchFromLocation(
                materialBatch.getLocation().getId(),
                materialBatch.getCurrentQuantity()
        );
    }
}
```

#### Di chuyển lô
```java
@Override
@Transactional
public void moveToLocation(Long batchId, Long newLocationId) {
    // ... validate ...
    
    // Tự động di chuyển capacity giữa các vị trí
    locationCapacityService.moveBatch(oldLocationId, newLocationId, quantity);
}
```

#### Cập nhật số lượng
```java
@Override
@Transactional
public void updateQuantity(Long batchId, BigDecimal newQuantity) {
    // ... validate ...
    
    // Tự động cập nhật capacity khi thay đổi số lượng
    if (materialBatch.getLocation() != null) {
        locationCapacityService.updateBatchQuantity(
                materialBatch.getLocation().getId(),
                oldQuantity,
                newQuantity
        );
    }
}
```

## API Endpoints

### 1. Lấy thông tin sức chứa đầy đủ
```
GET /location/{id}/capacity-info
```

**Response:**
```json
{
  "id": 1,
  "locationCode": "A-01-01",
  "warehouseName": "Kho chính",
  "maxCapacity": 1000.0,
  "currentCapacity": 750.0,
  "availableCapacity": 250.0,
  "occupancyPercentage": 75.0,
  "isAvailable": true,
  "status": "Đang đầy dần"
}
```

### 2. Tính lại sức chứa
```
POST /location/{id}/recalculate-capacity
```

**Phân quyền:** ADMIN, MANAGER

**Response:**
```json
{
  "message": "Đã tính lại sức chứa thành công",
  "locationCode": "A-01-01",
  "currentCapacity": 750.0,
  "availableCapacity": 250.0,
  "occupancyPercentage": 75.0,
  "status": "Đang đầy dần"
}
```

### 3. Lấy sức chứa còn trống
```
GET /location/{id}/available-capacity
```

**Response:**
```json
{
  "locationId": 1,
  "locationCode": "A-01-01",
  "availableCapacity": 250.0,
  "maxCapacity": 1000.0,
  "displayText": "250.00 / 1000.00 đơn vị",
  "hasSpace": true
}
```

### 4. Lấy tỷ lệ lấp đầy
```
GET /location/{id}/occupancy-percentage
```

**Response:**
```json
{
  "locationId": 1,
  "locationCode": "A-01-01",
  "percentage": 75.0,
  "status": "Đang đầy dần",
  "color": "orange",
  "isNearFull": false
}
```

## Trạng thái sức chứa

| Tỷ lệ lấp đầy | Trạng thái | Màu | Mô tả |
|--------------|-----------|-----|-------|
| 0% | Trống | Gray | Vị trí chưa có gì |
| 0-10% | Gần trống | Green | Còn rất nhiều chỗ |
| 10-30% | Còn nhiều chỗ | Green | An toàn |
| 30-60% | Vừa phải | Yellow | Bình thường |
| 60-80% | Đang đầy dần | Orange | Cần chú ý |
| 80-95% | Gần đầy | Red | Cảnh báo |
| >= 95% | Đầy | Red | Cần xử lý |

## Validation và Kiểm tra

### 1. Kiểm tra vượt quá sức chứa

Khi thêm lô vào vị trí, hệ thống tự động kiểm tra:

```java
if (location.getMaxCapacity() != null && newCapacity > location.getMaxCapacity()) {
    throw new MyCustomException(
        "Vị trí kho không đủ sức chứa. Còn trống: " + available + ", Cần: " + quantity
    );
}
```

### 2. Tự động cập nhật trạng thái khả dụng

```java
// Khi đầy
if (newCapacity >= location.getMaxCapacity()) {
    location.setIsAvailable(false);
}

// Khi có chỗ trống
if (newCapacity < location.getMaxCapacity()) {
    location.setIsAvailable(true);
}
```

## Tính năng nâng cao

### 1. Transaction Safety

Tất cả các thao tác đều được bảo vệ bởi `@Transactional`:
- Nếu có lỗi, toàn bộ thao tác sẽ rollback
- Đảm bảo dữ liệu luôn nhất quán

### 2. Null Safety

Service xử lý an toàn với các giá trị null:
- Location không có maxCapacity: không giới hạn
- CurrentCapacity null: mặc định = 0

### 3. Logging

Mọi thay đổi capacity đều được ghi log:
```
INFO: Đã thêm 100.0 vào vị trí 'A-01-01'. Capacity hiện tại: 750.0 / 1000.0
INFO: Đã di chuyển 50.0 từ vị trí 1 đến vị trí 2
INFO: Đã tính lại capacity cho vị trí 'A-01-01': 700.0 -> 750.0 (từ 5 lô)
```

### 4. Recovery

Nếu phát hiện sai lệch dữ liệu, có thể tính lại capacity:
- Quét tất cả lô tại vị trí
- Tính tổng currentQuantity
- Cập nhật lại currentCapacity và isAvailable

## Lưu ý khi sử dụng

1. **Không nên cập nhật trực tiếp currentCapacity của Location** - luôn sử dụng LocationCapacityService

2. **Khi import dữ liệu cũ** - chạy recalculateLocationCapacity cho tất cả vị trí:
```java
locationService.recalculateLocationCapacity(locationId);
```

3. **Monitoring** - theo dõi các vị trí có occupancyPercentage >= 80%

4. **Performance** - với số lượng lô lớn, recalculate có thể chậm, nên chạy trong background job

## Ví dụ sử dụng

### Kịch bản 1: Tạo lô mới

```http
POST /material-batches
{
  "materialId": 1,
  "locationId": 5,
  "batchNumber": "LOT-2025-001",
  "receivedQuantity": 100,
  "currentQuantity": 100,
  "receivedDate": "2025-11-01"
}
```

**Kết quả:** Location ID 5 sẽ tự động tăng currentCapacity thêm 100

### Kịch bản 2: Di chuyển lô

```http
PUT /material-batches/123/move-location?newLocationId=7
```

**Kết quả:** 
- Location cũ giảm currentCapacity
- Location mới tăng currentCapacity

### Kịch bản 3: Cập nhật số lượng

```http
PUT /material-batches/123/quantity?quantity=150
```

**Kết quả:** Location tự động cập nhật capacity theo sự chênh lệch

### Kịch bản 4: Kiểm tra capacity trước khi nhập hàng

```http
GET /location/5/available-capacity
```

```json
{
  "availableCapacity": 250.0,
  "hasSpace": true
}
```

Nếu hasSpace = true và availableCapacity >= số lượng cần nhập → OK

## Troubleshooting

### Vấn đề: Capacity bị sai lệch

**Giải pháp:**
```http
POST /location/{id}/recalculate-capacity
```

### Vấn đề: Location báo đầy nhưng thực tế còn chỗ

**Nguyên nhân:** maxCapacity quá thấp hoặc có lô đã bị xóa nhưng chưa trừ capacity

**Giải pháp:**
1. Kiểm tra maxCapacity
2. Recalculate capacity

### Vấn đề: Không thể thêm lô vào vị trí

**Lỗi:** "Vị trí kho không đủ sức chứa"

**Giải pháp:**
1. Kiểm tra available capacity
2. Chọn vị trí khác
3. Hoặc tăng maxCapacity nếu hợp lý

## Tổng kết

Hệ thống Location Capacity Management cung cấp:
✅ Cập nhật real-time và tự động
✅ Transaction safety
✅ Validation đầy đủ
✅ API endpoints tiện lợi
✅ Logging và monitoring
✅ Recovery mechanism

Mọi thao tác với MaterialBatch đều tự động cập nhật capacity, đảm bảo dữ liệu luôn chính xác.
