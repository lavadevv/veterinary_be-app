# API Examples - Material Batch with Multiple Items

## 1. Create a new batch with multiple materials

### Request
```http
POST /api/materials/batches
Content-Type: application/json
```

```json
{
  "batchNumber": "BATCH-2025-11-001",
  "internalBatchCode": "INT-BATCH-2025-001",
  "receivedDate": "2025-11-01",
  "supplierId": 5,
  "manufacturerId": 10,
  "countryOfOrigin": "USA",
  "invoiceNumber": "INV-2025-001",
  "locationId": 1,
  "notes": "Lô nhập hàng tháng 11/2025 từ nhà cung cấp Pharmaco",
  "items": [
    {
      "materialId": 101,
      "internalItemCode": "INT-BATCH-2025-001-ITEM-001",
      "manufacturerBatchNumber": "VA-LOT-20251001",
      "manufacturingDate": "2025-10-01",
      "expiryDate": "2027-10-01",
      "receivedQuantity": 100.000,
      "unitPrice": 500000,
      "taxPercent": 10,
      "shelfLocation": "A-01-001",
      "coaNumber": "COA-VA-20251001",
      "notes": "Vitamin A - Chất lượng cao",
      "activeIngredients": [
        {
          "activeIngredientId": 1,
          "coaContentValue": 500000,
          "coaContentUnit": "IU/g",
          "coaMinValue": 475000,
          "coaMaxValue": 525000,
          "coaNotes": "Theo tiêu chuẩn USP"
        }
      ]
    },
    {
      "materialId": 102,
      "internalItemCode": "INT-BATCH-2025-001-ITEM-002",
      "manufacturerBatchNumber": "VD3-LOT-20251015",
      "manufacturingDate": "2025-10-15",
      "expiryDate": "2027-10-15",
      "receivedQuantity": 50.000,
      "unitPrice": 800000,
      "taxPercent": 10,
      "shelfLocation": "A-01-002",
      "coaNumber": "COA-VD3-20251015",
      "notes": "Vitamin D3 - Hàng nhập khẩu Mỹ",
      "activeIngredients": [
        {
          "activeIngredientId": 2,
          "coaContentValue": 40000000,
          "coaContentUnit": "IU/g",
          "coaMinValue": 38000000,
          "coaMaxValue": 42000000,
          "coaNotes": "Vitamin D3 (Cholecalciferol) theo USP"
        }
      ]
    },
    {
      "materialId": 103,
      "internalItemCode": "INT-BATCH-2025-001-ITEM-003",
      "manufacturerBatchNumber": "VE-LOT-20251020",
      "manufacturingDate": "2025-10-20",
      "expiryDate": "2027-10-20",
      "receivedQuantity": 75.000,
      "unitPrice": 1200000,
      "taxPercent": 10,
      "locationId": 2,
      "shelfLocation": "B-02-001",
      "coaNumber": "COA-VE-20251020",
      "notes": "Vitamin E - Yêu cầu bảo quản mát",
      "activeIngredients": [
        {
          "activeIngredientId": 3,
          "coaContentValue": 50,
          "coaContentUnit": "%",
          "coaMinValue": 48,
          "coaMaxValue": 52,
          "coaNotes": "dl-alpha-Tocopheryl acetate"
        }
      ]
    }
  ]
}
```

### Response (201 Created)
```json
{
  "id": 1001,
  "batchNumber": "BATCH-2025-11-001",
  "internalBatchCode": "INT-BATCH-2025-001",
  "receivedDate": "2025-11-01",
  "supplier": {
    "id": 5,
    "supplierCode": "PHARMACO",
    "supplierName": "Pharmaco International Ltd."
  },
  "manufacturer": {
    "id": 10,
    "manufacturerCode": "DSM",
    "manufacturerName": "DSM Nutritional Products"
  },
  "countryOfOrigin": "USA",
  "invoiceNumber": "INV-2025-001",
  "location": {
    "id": 1,
    "locationCode": "WH01-A01",
    "locationName": "Kho A - Khu 01",
    "warehouse": {
      "id": 1,
      "warehouseCode": "WH01",
      "warehouseName": "Kho nguyên liệu chính"
    }
  },
  "totalAmount": 198000000.00,
  "totalItemsCount": 3,
  "batchStatus": "ACTIVE",
  "notes": "Lô nhập hàng tháng 11/2025 từ nhà cung cấp Pharmaco",
  "items": [
    {
      "id": 2001,
      "material": {
        "id": 101,
        "materialCode": "VIT-A-001",
        "materialName": "Vitamin A",
        "internationalName": "Retinyl Acetate",
        "unitOfMeasure": {
          "id": 1,
          "unitCode": "KG",
          "unitName": "Kilogram"
        },
        "categoryName": "Vitamin"
      },
      "internalItemCode": "INT-BATCH-2025-001-ITEM-001",
      "manufacturerBatchNumber": "VA-LOT-20251001",
      "manufacturingDate": "2025-10-01",
      "expiryDate": "2027-10-01",
      "receivedQuantity": 100.000,
      "currentQuantity": 100.000,
      "reservedQuantity": 0.000,
      "availableQuantity": 100.000,
      "unitPrice": 500000.00,
      "taxPercent": 10.0000,
      "subtotalAmount": 50000000.00,
      "taxAmount": 5000000.00,
      "totalAmount": 55000000.00,
      "testStatus": "CHO_KIEM_NGHIEM",
      "testStatusDisplay": "Chờ kiểm nghiệm",
      "usageStatus": "CACH_LY",
      "usageStatusDisplay": "Cách ly",
      "location": {
        "id": 1,
        "locationCode": "WH01-A01",
        "locationName": "Kho A - Khu 01",
        "warehouse": {
          "id": 1,
          "warehouseCode": "WH01",
          "warehouseName": "Kho nguyên liệu chính"
        }
      },
      "shelfLocation": "A-01-001",
      "coaNumber": "COA-VA-20251001",
      "qualificationStatus": "Chưa có dữ liệu",
      "isQualified": null,
      "notes": "Vitamin A - Chất lượng cao",
      "activeIngredients": [
        {
          "id": 3001,
          "activeIngredient": {
            "id": 1,
            "ingredientCode": "AI-VA",
            "ingredientName": "Vitamin A (Retinyl Acetate)",
            "chemicalFormula": "C22H32O2"
          },
          "coaContentValue": 500000,
          "coaContentUnit": "IU/g",
          "coaMinValue": 475000,
          "coaMaxValue": 525000,
          "coaNotes": "Theo tiêu chuẩn USP",
          "testContentValue": null,
          "testContentUnit": null,
          "testDate": null,
          "testMethod": null,
          "testNotes": null,
          "isQualified": null,
          "deviationPercentage": null,
          "qualificationStatus": "Chưa có dữ liệu"
        }
      ]
    },
    {
      "id": 2002,
      "material": {
        "id": 102,
        "materialCode": "VIT-D3-001",
        "materialName": "Vitamin D3",
        "internationalName": "Cholecalciferol",
        "unitOfMeasure": {
          "id": 1,
          "unitCode": "KG",
          "unitName": "Kilogram"
        },
        "categoryName": "Vitamin"
      },
      "internalItemCode": "INT-BATCH-2025-001-ITEM-002",
      "manufacturerBatchNumber": "VD3-LOT-20251015",
      "manufacturingDate": "2025-10-15",
      "expiryDate": "2027-10-15",
      "receivedQuantity": 50.000,
      "currentQuantity": 50.000,
      "reservedQuantity": 0.000,
      "availableQuantity": 50.000,
      "unitPrice": 800000.00,
      "taxPercent": 10.0000,
      "subtotalAmount": 40000000.00,
      "taxAmount": 4000000.00,
      "totalAmount": 44000000.00,
      "testStatus": "CHO_KIEM_NGHIEM",
      "testStatusDisplay": "Chờ kiểm nghiệm",
      "usageStatus": "CACH_LY",
      "usageStatusDisplay": "Cách ly",
      "shelfLocation": "A-01-002",
      "coaNumber": "COA-VD3-20251015",
      "qualificationStatus": "Chưa có dữ liệu",
      "isQualified": null,
      "notes": "Vitamin D3 - Hàng nhập khẩu Mỹ",
      "activeIngredients": [
        {
          "id": 3002,
          "activeIngredient": {
            "id": 2,
            "ingredientCode": "AI-VD3",
            "ingredientName": "Vitamin D3 (Cholecalciferol)",
            "chemicalFormula": "C27H44O"
          },
          "coaContentValue": 40000000,
          "coaContentUnit": "IU/g",
          "coaMinValue": 38000000,
          "coaMaxValue": 42000000,
          "coaNotes": "Vitamin D3 (Cholecalciferol) theo USP",
          "isQualified": null,
          "qualificationStatus": "Chưa có dữ liệu"
        }
      ]
    },
    {
      "id": 2003,
      "material": {
        "id": 103,
        "materialCode": "VIT-E-001",
        "materialName": "Vitamin E",
        "internationalName": "dl-alpha-Tocopheryl acetate",
        "unitOfMeasure": {
          "id": 1,
          "unitCode": "KG",
          "unitName": "Kilogram"
        },
        "categoryName": "Vitamin"
      },
      "internalItemCode": "INT-BATCH-2025-001-ITEM-003",
      "manufacturerBatchNumber": "VE-LOT-20251020",
      "manufacturingDate": "2025-10-20",
      "expiryDate": "2027-10-20",
      "receivedQuantity": 75.000,
      "currentQuantity": 75.000,
      "reservedQuantity": 0.000,
      "availableQuantity": 75.000,
      "unitPrice": 1200000.00,
      "taxPercent": 10.0000,
      "subtotalAmount": 90000000.00,
      "taxAmount": 9000000.00,
      "totalAmount": 99000000.00,
      "testStatus": "CHO_KIEM_NGHIEM",
      "testStatusDisplay": "Chờ kiểm nghiệm",
      "usageStatus": "CACH_LY",
      "usageStatusDisplay": "Cách ly",
      "location": {
        "id": 2,
        "locationCode": "WH01-B02",
        "locationName": "Kho B - Khu 02 (Bảo quản lạnh)",
        "warehouse": {
          "id": 1,
          "warehouseCode": "WH01",
          "warehouseName": "Kho nguyên liệu chính"
        }
      },
      "shelfLocation": "B-02-001",
      "coaNumber": "COA-VE-20251020",
      "qualificationStatus": "Chưa có dữ liệu",
      "isQualified": null,
      "notes": "Vitamin E - Yêu cầu bảo quản mát",
      "activeIngredients": [
        {
          "id": 3003,
          "activeIngredient": {
            "id": 3,
            "ingredientCode": "AI-VE",
            "ingredientName": "Vitamin E (Tocopheryl acetate)",
            "chemicalFormula": "C31H52O3"
          },
          "coaContentValue": 50,
          "coaContentUnit": "%",
          "coaMinValue": 48,
          "coaMaxValue": 52,
          "coaNotes": "dl-alpha-Tocopheryl acetate",
          "isQualified": null,
          "qualificationStatus": "Chưa có dữ liệu"
        }
      ]
    }
  ],
  "createdBy": "admin",
  "createdDate": "2025-11-01 08:30:00",
  "lastModifiedBy": "admin",
  "lastModifiedDate": "2025-11-01 08:30:00"
}
```

## 2. Get batch details

### Request
```http
GET /api/materials/batches/1001
```

### Response (200 OK)
Same structure as Create response above.

## 3. Update test results for a batch item

### Request
```http
PUT /api/materials/batches/1001/items/2001/test-results
Content-Type: application/json
```

```json
{
  "testStatus": "DA_KIEM_NGHIEM",
  "testReportNumber": "RPT-2025-11-001",
  "activeIngredients": [
    {
      "activeIngredientId": 1,
      "testContentValue": 498500,
      "testContentUnit": "IU/g",
      "testDate": "2025-11-05",
      "testMethod": "HPLC",
      "testNotes": "Kết quả đạt chuẩn USP"
    }
  ]
}
```

### Response (200 OK)
```json
{
  "id": 2001,
  "testStatus": "DA_KIEM_NGHIEM",
  "testStatusDisplay": "Đã kiểm nghiệm",
  "testReportNumber": "RPT-2025-11-001",
  "qualificationStatus": "Đạt",
  "isQualified": true,
  "activeIngredients": [
    {
      "id": 3001,
      "activeIngredient": {
        "id": 1,
        "ingredientCode": "AI-VA",
        "ingredientName": "Vitamin A (Retinyl Acetate)"
      },
      "coaContentValue": 500000,
      "coaContentUnit": "IU/g",
      "coaMinValue": 475000,
      "coaMaxValue": 525000,
      "testContentValue": 498500,
      "testContentUnit": "IU/g",
      "testDate": "2025-11-05",
      "testMethod": "HPLC",
      "testNotes": "Kết quả đạt chuẩn USP",
      "isQualified": true,
      "deviationPercentage": -0.3000,
      "qualificationStatus": "Đạt"
    }
  ]
}
```

## 4. Approve batch item for use

### Request
```http
PUT /api/materials/batches/1001/items/2001/approve
Content-Type: application/json
```

```json
{
  "usageStatus": "SAN_SANG",
  "notes": "Đã kiểm nghiệm đạt chuẩn, cho phép sử dụng"
}
```

### Response (200 OK)
```json
{
  "id": 2001,
  "usageStatus": "SAN_SANG",
  "usageStatusDisplay": "Sẵn sàng",
  "testStatus": "DA_KIEM_NGHIEM",
  "testStatusDisplay": "Đã kiểm nghiệm",
  "qualificationStatus": "Đạt",
  "isQualified": true,
  "notes": "Đã kiểm nghiệm đạt chuẩn, cho phép sử dụng"
}
```

## 5. List all batches

### Request
```http
GET /api/materials/batches?page=0&size=20&sort=receivedDate,desc
```

### Response (200 OK)
```json
{
  "content": [
    {
      "id": 1001,
      "batchNumber": "BATCH-2025-11-001",
      "receivedDate": "2025-11-01",
      "supplier": {
        "id": 5,
        "supplierName": "Pharmaco International Ltd."
      },
      "invoiceNumber": "INV-2025-001",
      "totalAmount": 198000000.00,
      "totalItemsCount": 3,
      "batchStatus": "ACTIVE"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

## 6. Find available stock by material

### Request
```http
GET /api/materials/101/available-stock
```

### Response (200 OK)
```json
{
  "materialId": 101,
  "materialCode": "VIT-A-001",
  "materialName": "Vitamin A",
  "totalAvailableQuantity": 100.000,
  "unitOfMeasure": "KG",
  "availableBatches": [
    {
      "batchItemId": 2001,
      "batchNumber": "BATCH-2025-11-001",
      "internalItemCode": "INT-BATCH-2025-001-ITEM-001",
      "manufacturerBatchNumber": "VA-LOT-20251001",
      "expiryDate": "2027-10-01",
      "availableQuantity": 100.000,
      "location": "WH01-A01",
      "shelfLocation": "A-01-001"
    }
  ]
}
```
