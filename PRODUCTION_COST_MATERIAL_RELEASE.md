# Production Cost Material Release Order (Lệnh xuất vật liệu)

## 📋 Overview
API để tạo "Lệnh xuất vật liệu" dựa trên ProductionCostSheet của từng sản phẩm trong Lot.
Hiển thị tất cả materials, labor, và energy items cần thiết cho sản xuất.

---

## 🎯 Business Logic

### Data Flow:
```
ProductionLot
  └─ ProductionPlan (nhiều plans)
       └─ ProductionPlanProduct (nhiều products)
            └─ ProductionCostSheet
                 └─ ProductionCostItem (MATERIAL / LABOR / ENERGY)
```

### Calculation:
```
scaleFactor = plannedQuantity / specUnits

For each ProductionCostItem:
  scaledQuantity = baseQuantity × scaleFactor

Example:
- Planned: 100 hộp
- SpecUnits: 10 (1 cost sheet = 10 hộp)
- Base quantity: 50 cái
→ Scaled quantity = (100 / 10) × 50 = 500 cái
```

---

## 🔌 API Endpoint

### **GET** `/production/plans/lots/{lotId}/cost-materials`

**Path Parameters:**
- `lotId` (Long, required): Production lot ID

**Response:** `ProductionCostMaterialDto`

```json
{
  "lotId": 5,
  "lotNumber": "021125",
  "formulaCode": "mentroll",
  "formulaName": "Men trộn",
  "totalBatchSize": 110,
  "batchUnit": "kg",
  
  "productCosts": [
    {
      "productId": 1,
      "productCode": "lactozyme",
      "productName": "Lactozyme",
      "plannedQuantity": 100,
      "unitOfMeasure": "Hộp",
      
      "costSheet": {
        "id": 1,
        "sheetCode": "LACTOZYME_001",
        "sheetName": "Lactozyme (Daeyong) 1.000g 1/1",
        "specUnits": 10
      },
      
      "items": [
        {
          "orderNo": 1,
          "itemType": "MATERIAL",
          "itemCode": "MAT001",
          "itemName": "Tem Lactozyme KT15x25",
          "unit": "cái",
          "baseQuantity": 50,
          "scaledQuantity": 500,
          "scaleFactor": 10.0,
          "notes": null
        },
        {
          "orderNo": 2,
          "itemType": "LABOR",
          "itemCode": "LABOR001",
          "itemName": "Công nhân đóng gói",
          "unit": "giờ",
          "baseQuantity": 1,
          "scaledQuantity": 10,
          "scaleFactor": 10.0,
          "notes": null
        },
        {
          "orderNo": 3,
          "itemType": "ENERGY",
          "itemCode": "ENERGY001",
          "itemName": "Điện năng sản xuất",
          "unit": "kWh",
          "baseQuantity": 5,
          "scaledQuantity": 50,
          "scaleFactor": 10.0,
          "notes": null
        }
      ]
    }
  ],
  
  "summary": {
    "totalProducts": 3,
    "totalItems": 15,
    "materialItems": 10,
    "laborItems": 3,
    "energyItems": 2
  }
}
```

---

## 📊 Frontend Display Format

### Header:
```
LỆNH XUẤT VẬT LIỆU: Men trộn | CỠ LÔ: 110kg | SỐ LÔ: 021125
```

### Table (with rowspan for product headers):

```
┌──────────────────────────────────────────────────────────────────────────┐
│  Lactozyme (Daeyong) 1.000g 1/1 - Số lượng: 100 Hộp                     │ ← Product Header
├─────┬──────────────┬─────────────────────────────────┬─────┬──────────┤
│ STT │  Mã vật liệu │  Tên vật liệu                   │ ĐVT │ Số lượng │
├─────┼──────────────┼─────────────────────────────────┼─────┼──────────┤
│  1  │ MAT001       │ Tem Lactozyme KT15x25          │ cái │    500   │
│  2  │ LABOR001     │ Công nhân đóng gói             │ giờ │    10    │
│  3  │ ENERGY001    │ Điện năng sản xuất             │ kWh │    50    │
├─────┴──────────────┴─────────────────────────────────┴─────┴──────────┤
│  Protein Plus 500g - Số lượng: 50 Hộp                                  │ ← Next Product
├─────┬──────────────┬─────────────────────────────────┬─────┬──────────┤
│  4  │ MAT002       │ Nhãn Protein                   │ cái │    250   │
│  5  │ LABOR002     │ Công đóng gói protein          │ giờ │    5     │
```

---

## 🏗️ Implementation Details

### Files Created:
1. **DTO:**
   - `ProductionCostMaterialDto.java` - Response DTO với nested classes

### Files Modified:
1. **Service Interface:**
   - `ProductionPlanService.java` - Added `getProductionCostMaterials(Long lotId)`

2. **Service Implementation:**
   - `ProductionPlanServiceImpl.java`
     - Injected: MaterialRepository, LaborRateRepository, EnergyTariffRepository
     - Implemented: `getProductionCostMaterials()` với batch loading
     - Helper: `ProductWithCostSheet` record

3. **Controller:**
   - `ProductionPlanController.java` - Added GET endpoint

4. **Frontend Service:**
   - `production-plan.service.js` - Added `getProductionCostMaterials(lotId)`

---

## 🚀 Performance Optimizations

### Batch Loading Strategy:
1. **First Pass**: Collect all IDs
   - Cost sheet IDs
   - Material IDs
   - Labor rate IDs
   - Energy tariff IDs

2. **Batch Queries**: Load all masters in bulk
   - `costSheetRepository.findAllById(costSheetIds)` → với @EntityGraph
   - `materialRepository.findAllById(materialIds)`
   - `laborRateRepository.findAllById(laborRateIds)`
   - `energyTariffRepository.findAllById(energyTariffIds)`

3. **Map Building**: Create hash maps for O(1) lookup
   - `Map<Long, ProductionCostSheet>`
   - `Map<Long, Material>`
   - `Map<Long, LaborRate>`
   - `Map<Long, EnergyTariff>`

4. **Single Pass Processing**: Loop through products once

### Result:
- **Before**: N products × M items = N×M queries (worst case)
- **After**: 4 batch queries + O(N) processing = **Constant query count**

---

## ✅ Testing Checklist

### Backend Tests:
- [ ] Test with lot có 1 product
- [ ] Test với lot có nhiều products
- [ ] Test với products có different cost sheets
- [ ] Test với cost sheet chứa MATERIAL items
- [ ] Test với cost sheet chứa LABOR items  
- [ ] Test với cost sheet chứa ENERGY items
- [ ] Test với cost sheet chứa mixed items
- [ ] Test với product không có cost sheet
- [ ] Test scale factor calculation (plannedQuantity / specUnits)
- [ ] Test với specUnits = 0 hoặc null
- [ ] Test với cost items không có master data

### Frontend Tests:
- [ ] Service call getProductionCostMaterials()
- [ ] Parse response correctly
- [ ] Display header với formula info
- [ ] Display product headers với rowspan
- [ ] Display cost items correctly
- [ ] Format quantities with decimals
- [ ] Handle empty items
- [ ] Handle loading state
- [ ] Handle errors

---

## 📝 Notes

### Key Differences vs Material Consumption:
1. **Source**: ProductionCostSheet (không phải Formula)
2. **Items**: Material + Labor + Energy (không chỉ materials)
3. **Grouping**: Theo Product (không theo Formula)
4. **Calculation**: Scale theo plannedQuantity/specUnits (không FIFO)
5. **No Inventory**: Không check stock availability

### Future Enhancements:
- [ ] Add actual release tracking (thực xuất)
- [ ] Add cost calculation (unit price × quantity)
- [ ] Add packaging materials
- [ ] Add quality control items
- [ ] Export to Excel/PDF
- [ ] Barcode generation for picking

---

## 🔗 Related APIs

- **Material Consumption**: `/production/plans/simulate-material-consumption`
  - FIFO picking từ MaterialBatch inventory
  - Only materials, no labor/energy
  - Grouped by formula

- **Cost Materials**: `/production/plans/lots/{lotId}/cost-materials`
  - All cost items (material + labor + energy)
  - Grouped by product
  - No inventory check

---

## 📚 References

- **ProductionCostSheet**: `modules/pcost/model/ProductionCostSheet.java`
- **ProductionCostItem**: `modules/pcost/model/ProductionCostItem.java`
- **CostItemType Enum**: MATERIAL, LABOR, ENERGY, OVERHEAD, OTHER
- **Material**: `modules/material/model/Material.java`
- **LaborRate**: `modules/pcost/model/LaborRate.java`
- **EnergyTariff**: `modules/pcost/model/EnergyTariff.java`
