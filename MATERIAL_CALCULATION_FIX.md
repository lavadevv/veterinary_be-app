# Material Calculation Fix - Unit Conversion

## Vấn đề phát hiện
Khi `batchSize = 100 kg` nhưng công thức tính theo gram (g), hệ thống tính sai vì:
1. Không convert đơn vị `kg → g` trước khi tính
2. Nhân `batchSize` hai lần trong công thức
3. Không nhất quán giữa `batchUnit` và `basisUnit`

## Ví dụ lỗi cũ:

### Formula:
- `basisValue = 1000g`
- `basisUnit = "g"`
- Material percentage = 5%

### Input:
- `batchSize = 100`
- `batchUnit = "kg"`

### Tính sai (cũ):
```java
scalingFactor = batchSize / basisValue 
              = 100 / 1000 
              = 0.1

requiredQuantity = (percentage / 100) × batchSize × scalingFactor
                 = 0.05 × 100 × 0.1
                 = 0.5g  ❌ SAI!
```

**Kết quả sai:** 0.5g cho 100kg batch!

## Sửa đổi:

### Bước 1: Convert unit
```java
batchSizeInBasisUnit = convertToUnit(100, "kg", "g")
                     = 100,000g
```

### Bước 2: Tính scaling factor
```java
scalingFactor = batchSizeInBasisUnit / basisValue
              = 100,000 / 1000
              = 100
```

### Bước 3: Tính required quantity
```java
requiredQuantity = (percentage / 100) × scalingFactor × basisValue
                 = 0.05 × 100 × 1000
                 = 5,000g ✅ ĐÚNG!
```

**Kết quả đúng:** 5,000g (5kg) cho 100kg batch với 5% material!

## Các thay đổi code:

### 1. Thêm method `convertToUnit()`
```java
private BigDecimal convertToUnit(BigDecimal value, String fromUnit, String toUnit) {
    // Convert to base unit (g) first
    BigDecimal valueInGrams = switch (from) {
        case "kg" -> value.multiply(1000);
        case "g" -> value;
        case "mg" -> value.divide(1000);
        case "ton" -> value.multiply(1_000_000);
        default -> value;
    };
    
    // Convert from base unit to target
    return switch (to) {
        case "kg" -> valueInGrams.divide(1000);
        case "g" -> valueInGrams;
        case "mg" -> valueInGrams.multiply(1000);
        case "ton" -> valueInGrams.divide(1_000_000);
        default -> valueInGrams;
    };
}
```

### 2. Sửa `calculateMaterialRequirements()`
```java
// OLD ❌
BigDecimal scalingFactor = batchSize.divide(basisValue, 6, RoundingMode.HALF_UP);
BigDecimal requiredQuantityG = percentage
    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
    .multiply(batchSize)      // ❌ Nhân batchSize 2 lần!
    .multiply(scalingFactor)
    .setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);

// NEW ✅
BigDecimal batchSizeInBasisUnit = convertToUnit(batchSize, batchUnit, basisUnit);
BigDecimal scalingFactor = batchSizeInBasisUnit.divide(basisValue, 6, RoundingMode.HALF_UP);
BigDecimal requiredQuantityInBasisUnit = percentage
    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
    .multiply(scalingFactor)
    .multiply(basisValue)     // ✅ Đúng công thức!
    .setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
```

### 3. Sửa `simulateMaterialConsumption()`
Áp dụng tương tự logic ở trên.

## Test Cases:

### Case 1: 100kg batch, 5% material, basis 1000g
```
Input:
- batchSize = 100 kg
- basisValue = 1000g
- percentage = 5%

Expected:
- scalingFactor = 100
- requiredQuantity = 5,000g (5kg)
```

### Case 2: 10kg batch, 2% material, basis 1000g
```
Input:
- batchSize = 10 kg
- basisValue = 1000g
- percentage = 2%

Expected:
- scalingFactor = 10
- requiredQuantity = 200g
```

### Case 3: 1000g batch (same unit), 10% material, basis 1000g
```
Input:
- batchSize = 1000 g
- basisValue = 1000g
- percentage = 10%

Expected:
- scalingFactor = 1
- requiredQuantity = 100g
```

## Files Changed:
1. ✅ `ProductionPlanServiceImpl.java`
   - Added `convertToUnit()` method
   - Fixed `calculateMaterialRequirements()`
   - Fixed `simulateMaterialConsumption()`

2. ✅ `SimulateMaterialConsumptionRequest.java`
   - Added `formulaCode` field
   - Made `formulaId` optional

3. ✅ `ProductFormulaRepository.java`
   - Added `findByHeaderFormulaCodeAndIsActiveTrue()`

4. ✅ `ProductionPlanDetailPage.vue`
   - Support multiple formulas in one lot
   - Group by formulaCode + batchSize
   - Display multiple material consumption tables

## Verification:
Sau khi restart backend, kiểm tra:
1. ✅ 100kg batch phải tính đúng = 100,000g
2. ✅ Material requirement phải khớp với phần trăm
3. ✅ FIFO picking phải đúng số lượng
4. ✅ Summary cost phải chính xác
