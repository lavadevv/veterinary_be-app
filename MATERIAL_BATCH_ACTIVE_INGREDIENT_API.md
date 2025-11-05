# Material Batch Active Ingredient API Documentation

## 📚 Tổng quan

API endpoints để quản lý thông tin hoạt chất của từng lô nguyên liệu, bao gồm:
- Hàm lượng theo COA (Certificate of Analysis)
- Hàm lượng thực tế KQPT (Kết quả phân tích)
- Kiểm tra đạt/không đạt chuẩn (90% ≤ KQPT/COA ≤ 110%)

Base URL: `/material-batches`

---

## 🎯 API Endpoints

### 1. **Lấy thông tin chi tiết lô (bao gồm hoạt chất)**

```http
GET /material-batches/{batchId}/details
```

**Response:**
```json
{
  "code": 1,
  "message": "Success",
  "data": {
    "id": 1,
    "materialCode": "VA-001",
    "materialName": "Vitamin A Powder",
    "internationalName": "Retinol",
    "unitOfMeasure": "kg",
    "batchNumber": "LOT-VA-2024-001",
    "internalBatchCode": "VA-001-2024-001",
    "supplierName": "ABC Pharma",
    "manufacturerName": "XYZ Corp",
    "locationCode": "KHO-A",
    "shelfLocation": "A-01-05",
    "coaFilePath": "/uploads/coa/LOT-VA-2024-001.pdf",
    "imagePath": "/uploads/batches/LOT-VA-2024-001.jpg",
    "overallQualificationStatus": "Đạt",
    "isAllQualified": true,
    "batchActiveIngredients": [
      {
        "id": 1,
        "activeIngredientName": "Retinyl Acetate",
        "coaContent": 500000,
        "coaContentUnit": "IU/g",
        "actualContent": 485000,
        "actualContentUnit": "IU/g",
        "ratio": 97.00,
        "qualificationStatus": "Đạt",
        "isQualified": true
      }
    ]
  }
}
```

### 2. **Lấy tất cả lô với thông tin đầy đủ**

```http
GET /material-batches/details
```

**Response:** 
```json
{
  "code": 1,
  "message": "Success",
  "data": [...],
  "totalRecord": 50
}
```

---

### 3. **Lấy lô có hoạt chất không đạt chuẩn**

```http
GET /material-batches/unqualified
```

**Response:**
```json
{
  "code": 1,
  "message": "Success",
  "data": [
    {
      "id": 5,
      "materialCode": "AMX-002",
      "materialName": "Amoxicillin Trihydrate",
      "overallQualificationStatus": "Không đạt",
      "isAllQualified": false,
      "unqualifiedIngredients": ["Clavulanic Acid"],
      "batchActiveIngredients": [...]
    }
  ],
  "totalRecord": 5
}
```

---

### 4. **Lấy danh sách hoạt chất của một lô**

```http
GET /material-batches/{batchId}/active-ingredients
```

**Response:**
```json
{
  "code": 1,
  "message": "Success",
  "data": [...],
  "totalRecord": 3
}
```

---

### 5. **Thêm hoạt chất vào lô**

```http
POST /material-batches/{batchId}/active-ingredients
Content-Type: application/json
```

**Request Body:**
```json
{
  "activeIngredientId": 10,
  "coaContent": 500000,
  "coaContentUnit": "IU/g",
  "actualContent": 485000,
  "actualContentUnit": "IU/g",
  "notes": "Kết quả phân tích lần 1"
}
```

**Response:**
```json
{
  "code": 1,
  "message": "Success",
  "data": {
    "id": 1,
    "activeIngredientName": "Retinyl Acetate",
    "ratio": 97.00,
    "qualificationStatus": "Đạt",
    "isQualified": true
  }
}
```

---

### 6. **Thêm nhiều hoạt chất cùng lúc**

```http
POST /material-batches/{batchId}/active-ingredients/batch
Content-Type: application/json
```

**Request Body:**
```json
[
  {
    "activeIngredientId": 10,
    "coaContent": 500000,
    "coaContentUnit": "IU/g",
    "actualContent": 485000,
    "actualContentUnit": "IU/g"
  },
  {
    "activeIngredientId": 11,
    "coaContent": 1700000,
    "coaContentUnit": "IU/g",
    "actualContent": 1650000,
    "actualContentUnit": "IU/g"
  }
]
```

**Response:** 
```json
{
  "code": 1,
  "message": "Success",
  "data": [...],
  "totalRecord": 2
}
```

---

### 7. **Cập nhật thông tin hoạt chất**

```http
PUT /material-batches/active-ingredients/{id}
Content-Type: application/json
```

**Request Body:**
```json
{
  "activeIngredientId": 10,
  "coaContent": 500000,
  "coaContentUnit": "IU/g",
  "actualContent": 490000,
  "actualContentUnit": "IU/g",
  "notes": "Kết quả phân tích lần 2 (đã cập nhật)"
}
```

**Response:**
```json
{
  "code": 1,
  "message": "Success",
  "data": {
    "id": 1,
    "ratio": 98.00,
    "qualificationStatus": "Đạt"
  }
}
```

---

### 8. **Xóa hoạt chất khỏi lô**

```http
DELETE /material-batches/active-ingredients/{id}
```

**Response:**
```json
{
  "code": 1,
  "message": "Xóa hoạt chất khỏi lô thành công"
}
```

---

### 9. **Lấy danh sách hoạt chất không đạt chuẩn (raw)**

```http
GET /material-batches/active-ingredients/unqualified
```

**Response:** 
```json
{
  "code": 1,
  "message": "Success",
  "data": [...],
  "totalRecord": 10
}
```

---

## 📊 Business Logic

### Quy tắc kiểm tra đạt/không đạt:

```
KQPT/COA Ratio = (actualContent / coaContent) * 100

✅ ĐẠT:   90% ≤ ratio ≤ 110%
❌ KHÔNG ĐẠT: ratio < 90% hoặc ratio > 110%
⚠️  CHƯA CÓ DỮ LIỆU: actualContent hoặc coaContent = null
```

### Trạng thái lô (Overall Qualification):

- **"Đạt"**: TẤT CẢ hoạt chất đều đạt chuẩn
- **"Không đạt"**: Có ÍT NHẤT 1 hoạt chất không đạt
- **"Chưa có dữ liệu"**: Chưa có dữ liệu COA/KQPT cho bất kỳ hoạt chất nào

---

## 🔧 Validation Rules

### MaterialBatchActiveIngredientRequest:

```java
{
  "activeIngredientId": Long (required, not null),
  "coaContent": BigDecimal (optional, >= 0),
  "coaContentUnit": String (optional),
  "actualContent": BigDecimal (optional, >= 0),
  "actualContentUnit": String (optional),
  "notes": String (optional)
}
```

---

## 💾 Database Structure

### Table: `material_batch_active_ingredients`

```sql
CREATE TABLE material_batch_active_ingredients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_id BIGINT NOT NULL,
    active_ingredient_id BIGINT NOT NULL,
    coa_content DECIMAL(18,6),
    coa_content_unit VARCHAR(30),
    actual_content DECIMAL(18,6),
    actual_content_unit VARCHAR(30),
    notes TEXT,
    created_at DATETIME,
    created_by VARCHAR(100),
    updated_at DATETIME,
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_mbai_batch 
        FOREIGN KEY (batch_id) REFERENCES material_batches(id),
    CONSTRAINT fk_mbai_active_ingredient 
        FOREIGN KEY (active_ingredient_id) REFERENCES active_ingredients(id),
    CONSTRAINT uk_batch_active_ingredient 
        UNIQUE (batch_id, active_ingredient_id)
);
```

---

## 🎨 Frontend Integration Example

### Hiển thị bảng lô nguyên liệu:

```javascript
// Fetch batch details
const response = await axios.get(`/api/material-batches/${batchId}/details`);
const batch = response.data.data;

// Display table
<table>
  <thead>
    <tr>
      <th>Mã nguyên liệu</th>
      <th>Tên nguyên liệu</th>
      <th>Hoạt chất</th>
      <th>COA</th>
      <th>KQPT</th>
      <th>Tỷ lệ (%)</th>
      <th>Trạng thái</th>
      <th>Vị trí kệ</th>
      <th>Ảnh</th>
    </tr>
  </thead>
  <tbody>
    {batch.batchActiveIngredients.map(ingredient => (
      <tr key={ingredient.id}>
        <td>{batch.materialCode}</td>
        <td>{batch.materialName}</td>
        <td>{ingredient.activeIngredientName}</td>
        <td>{ingredient.coaContent} {ingredient.coaContentUnit}</td>
        <td>{ingredient.actualContent} {ingredient.actualContentUnit}</td>
        <td>{ingredient.ratio}%</td>
        <td>
          {ingredient.isQualified ? (
            <span class="badge-success">✅ Đạt</span>
          ) : (
            <span class="badge-danger">❌ Không đạt</span>
          )}
        </td>
        <td>{batch.shelfLocation}</td>
        <td>
          {batch.imagePath && (
            <img src={batch.imagePath} alt="Batch" width="50" />
          )}
        </td>
      </tr>
    ))}
  </tbody>
</table>

// Overall status
<div class="alert">
  <strong>Trạng thái tổng thể:</strong> {batch.overallQualificationStatus}
  {!batch.isAllQualified && batch.unqualifiedIngredients.length > 0 && (
    <div>
      <strong>Hoạt chất không đạt:</strong> {batch.unqualifiedIngredients.join(', ')}
    </div>
  )}
</div>
```

---

## 🧪 Test Cases

### 1. Thêm hoạt chất mới:
```bash
curl -X POST http://localhost:8080/material-batches/1/active-ingredients \
  -H "Content-Type: application/json" \
  -d '{
    "activeIngredientId": 10,
    "coaContent": 500000,
    "coaContentUnit": "IU/g",
    "actualContent": 485000,
    "actualContentUnit": "IU/g"
  }'
```

### 2. Lấy lô có vấn đề:
```bash
curl http://localhost:8080/material-batches/unqualified
```

### 3. Cập nhật KQPT:
```bash
curl -X PUT http://localhost:8080/material-batches/active-ingredients/1 \
  -H "Content-Type: application/json" \
  -d '{
    "actualContent": 490000,
    "actualContentUnit": "IU/g"
  }'
```

---

## ⚡ Performance Tips

1. **EntityGraph đã được cấu hình** để eager load quan hệ cần thiết
2. **Sử dụng pagination** khi lấy danh sách lớn
3. **Cache** kết quả nếu cần (Redis)
4. **Index** trên `batch_id` và `active_ingredient_id`

---

## 🚀 Next Steps

1. ✅ Migration SQL (tạo bảng mới)
2. ✅ Repository, Service, Controller
3. ✅ DTO & Mapper
4. 🔄 Frontend integration (Vue.js)
5. 🔄 Testing (Unit & Integration tests)
6. 🔄 API Documentation (Swagger/OpenAPI)

---

**Created:** 2024-11-01
**Version:** 1.0.0
