# Implementation Checklist - MaterialBatch Refactoring

## ✅ Hoàn thành

### 1. Entity Models
- [x] Tạo `MaterialBatchItem.java`
- [x] Tạo `MaterialBatchItemActiveIngredient.java`
- [x] Cập nhật `MaterialBatch.java`
- [x] Cập nhật `Material.java`

### 2. Repository Interfaces
- [x] Tạo `MaterialBatchItemRepository.java`
- [x] Tạo `MaterialBatchItemActiveIngredientRepository.java`

### 3. DTOs
- [x] Tạo `CreateMaterialBatchRequest.java`
- [x] Tạo `MaterialBatchResponse.java`

### 4. Documentation
- [x] Tạo `MATERIAL_BATCH_REFACTOR_GUIDE.md` - Hướng dẫn chi tiết
- [x] Tạo `MATERIAL_BATCH_REFACTOR_SUMMARY.md` - Tóm tắt thay đổi
- [x] Tạo `API_EXAMPLES_MATERIAL_BATCH.md` - Ví dụ API

### 5. Database Migration
- [x] Tạo `V1__refactor_material_batch_to_support_multiple_materials.sql`

## ⏳ Cần thực hiện tiếp

### 1. Backend Implementation

#### Service Layer
- [ ] Tạo/Cập nhật `MaterialBatchService.java`
  - [ ] Method: createBatch(CreateMaterialBatchRequest)
  - [ ] Method: getBatchById(Long)
  - [ ] Method: updateBatch(Long, UpdateMaterialBatchRequest)
  - [ ] Method: deleteBatch(Long)
  - [ ] Method: getBatches(Pageable, filters)
  
- [ ] Tạo/Cập nhật `MaterialBatchItemService.java`
  - [ ] Method: getItemById(Long)
  - [ ] Method: updateItemTestResults(Long, TestResultsRequest)
  - [ ] Method: approveItem(Long, ApprovalRequest)
  - [ ] Method: rejectItem(Long, RejectionRequest)
  - [ ] Method: getAvailableItemsByMaterial(Long materialId)
  - [ ] Method: allocateStock(AllocationRequest) - FIFO/FEFO
  
- [ ] Cập nhật `MaterialService.java`
  - [ ] Method: getAvailableStock(Long materialId)
  - [ ] Method: updateCurrentStock() - tính từ batchItems

#### Controller Layer
- [ ] Cập nhật `MaterialBatchController.java`
  - [ ] POST /api/materials/batches
  - [ ] GET /api/materials/batches/{id}
  - [ ] PUT /api/materials/batches/{id}
  - [ ] DELETE /api/materials/batches/{id}
  - [ ] GET /api/materials/batches
  - [ ] GET /api/materials/batches/{id}/items
  
- [ ] Tạo/Cập nhật `MaterialBatchItemController.java`
  - [ ] GET /api/materials/batches/{batchId}/items/{itemId}
  - [ ] PUT /api/materials/batches/{batchId}/items/{itemId}
  - [ ] PUT /api/materials/batches/{batchId}/items/{itemId}/test-results
  - [ ] PUT /api/materials/batches/{batchId}/items/{itemId}/approve
  - [ ] PUT /api/materials/batches/{batchId}/items/{itemId}/reject
  - [ ] DELETE /api/materials/batches/{batchId}/items/{itemId}

#### Additional DTOs
- [ ] Tạo `UpdateMaterialBatchRequest.java`
- [ ] Tạo `UpdateMaterialBatchItemRequest.java`
- [ ] Tạo `TestResultsRequest.java`
- [ ] Tạo `ApprovalRequest.java`
- [ ] Tạo `RejectionRequest.java`
- [ ] Tạo `AllocationRequest.java`
- [ ] Tạo `AvailableStockResponse.java`

#### Mappers
- [ ] Tạo `MaterialBatchMapper.java` (Entity <-> DTO)
- [ ] Tạo `MaterialBatchItemMapper.java`
- [ ] Tạo `MaterialBatchItemActiveIngredientMapper.java`

#### Validators
- [ ] Tạo `MaterialBatchValidator.java`
  - [ ] Validate batch có ít nhất 1 item
  - [ ] Validate không có material trùng trong cùng batch
  - [ ] Validate dates (manufacturing < expiry)
  - [ ] Validate quantities > 0
  
- [ ] Tạo `MaterialBatchItemValidator.java`
  - [ ] Validate active ingredients match material's definition
  - [ ] Validate COA ranges (min < max)
  - [ ] Validate test values in range

### 2. Database Migration

- [ ] **CRITICAL**: Backup database trước khi migrate
  ```bash
  mysqldump -u user -p database > backup_before_migration_$(date +%Y%m%d_%H%M%S).sql
  ```

- [ ] Review migration script
- [ ] Test migration trên database development
- [ ] Test migration trên database staging
- [ ] Run migration trên production

- [ ] Verify data integrity sau migration:
  - [ ] Check số lượng records: material_batches = material_batch_items
  - [ ] Check foreign keys valid
  - [ ] Check calculations (totalAmount)
  - [ ] Check active ingredients migrated correctly

### 3. Frontend Implementation

#### Components
- [ ] `MaterialBatchForm.vue` - Form tạo/sửa lô
  - [ ] Header section (batch info)
  - [ ] Items list/table
  - [ ] Add/Remove item functionality
  - [ ] Item detail form (material, quantity, price, etc.)
  - [ ] Active ingredients section per item
  
- [ ] `MaterialBatchList.vue` - Danh sách lô
  - [ ] Table hiển thị batches
  - [ ] Filters (date range, supplier, status)
  - [ ] Pagination
  - [ ] Actions (view, edit, delete)
  
- [ ] `MaterialBatchDetail.vue` - Chi tiết lô
  - [ ] Header info
  - [ ] Items table với expand/collapse
  - [ ] Quality control status per item
  - [ ] Documents/attachments
  
- [ ] `MaterialBatchItemTestResults.vue` - Nhập kết quả test
  - [ ] Form nhập test values
  - [ ] Compare với COA values
  - [ ] Display qualification status
  
- [ ] `MaterialBatchItemApproval.vue` - Phê duyệt item
  - [ ] Review test results
  - [ ] Approve/Reject actions
  - [ ] Comments/notes

#### API Integration
- [ ] Tạo `materialBatchApi.js`
  - [ ] createBatch()
  - [ ] getBatch(id)
  - [ ] updateBatch(id, data)
  - [ ] deleteBatch(id)
  - [ ] getBatches(params)
  
- [ ] Tạo `materialBatchItemApi.js`
  - [ ] getItem(batchId, itemId)
  - [ ] updateTestResults(batchId, itemId, data)
  - [ ] approveItem(batchId, itemId, data)
  - [ ] rejectItem(batchId, itemId, data)

#### Routes
- [ ] Cập nhật router
  ```javascript
  {
    path: '/materials/batches',
    name: 'MaterialBatchList',
    component: MaterialBatchList
  },
  {
    path: '/materials/batches/create',
    name: 'MaterialBatchCreate',
    component: MaterialBatchForm
  },
  {
    path: '/materials/batches/:id',
    name: 'MaterialBatchDetail',
    component: MaterialBatchDetail
  },
  {
    path: '/materials/batches/:id/edit',
    name: 'MaterialBatchEdit',
    component: MaterialBatchForm
  }
  ```

#### State Management (if using Vuex/Pinia)
- [ ] Tạo `materialBatchStore.js`
  - [ ] State: batches, currentBatch, loading, error
  - [ ] Actions: fetchBatches, createBatch, updateBatch, etc.
  - [ ] Getters: getBatchById, getItemsByBatch, etc.

### 4. Testing

#### Backend Tests
- [ ] Unit Tests - Entities
  - [ ] MaterialBatchTest.java
  - [ ] MaterialBatchItemTest.java
  - [ ] Test lifecycle methods (@PrePersist, @PreUpdate)
  - [ ] Test business methods
  
- [ ] Unit Tests - Repositories
  - [ ] MaterialBatchItemRepositoryTest.java
  - [ ] Test custom query methods
  - [ ] Test FIFO/FEFO queries
  
- [ ] Unit Tests - Services
  - [ ] MaterialBatchServiceTest.java
  - [ ] MaterialBatchItemServiceTest.java
  - [ ] Mock dependencies
  - [ ] Test business logic
  
- [ ] Integration Tests
  - [ ] Test full create batch flow
  - [ ] Test cascade operations
  - [ ] Test transaction rollback
  - [ ] Test concurrency scenarios
  
- [ ] API Tests
  - [ ] Test REST endpoints
  - [ ] Test request validation
  - [ ] Test error handling
  - [ ] Test pagination/filtering

#### Frontend Tests
- [ ] Unit Tests - Components
  - [ ] MaterialBatchForm.test.js
  - [ ] MaterialBatchList.test.js
  - [ ] Test user interactions
  - [ ] Test data binding
  
- [ ] E2E Tests
  - [ ] Test create batch flow
  - [ ] Test view/edit batch
  - [ ] Test add/remove items
  - [ ] Test test results input
  - [ ] Test approval flow

### 5. Performance Optimization

- [ ] Add database indexes (already in migration script)
- [ ] Optimize N+1 queries
  - [ ] Use EntityGraph for batch details
  - [ ] Batch fetch items and active ingredients
- [ ] Add caching where appropriate
  - [ ] Cache material data
  - [ ] Cache supplier/manufacturer data
- [ ] Optimize large batch handling
  - [ ] Pagination for items
  - [ ] Lazy loading
- [ ] Add database connection pooling config

### 6. Security & Validation

- [ ] Add authorization checks
  - [ ] WHO can create batches?
  - [ ] WHO can approve items?
  - [ ] WHO can view sensitive info?
- [ ] Input validation
  - [ ] Sanitize user inputs
  - [ ] Validate file uploads (COA, MSDS, etc.)
- [ ] Audit logging
  - [ ] Log batch creation
  - [ ] Log approval/rejection
  - [ ] Log test results updates

### 7. Documentation & Training

- [ ] Update API documentation (Swagger/OpenAPI)
- [ ] Create user manual
  - [ ] How to create batch with multiple items
  - [ ] How to input test results
  - [ ] How to approve/reject items
- [ ] Create training materials
  - [ ] Screenshots
  - [ ] Video tutorials
- [ ] Team training session
  - [ ] Backend developers
  - [ ] Frontend developers
  - [ ] QA team
  - [ ] End users

### 8. Deployment

- [ ] Code review
- [ ] Update CHANGELOG.md
- [ ] Tag release version
- [ ] Deploy to staging
  - [ ] Run smoke tests
  - [ ] User acceptance testing
- [ ] Deploy to production
  - [ ] Scheduled downtime notification
  - [ ] Run migration
  - [ ] Verify deployment
  - [ ] Monitor errors/logs
- [ ] Post-deployment
  - [ ] Verify data integrity
  - [ ] Monitor performance
  - [ ] Collect user feedback

## 📞 Support Contacts

- Backend Lead: [Name/Email]
- Frontend Lead: [Name/Email]
- Database Admin: [Name/Email]
- QA Lead: [Name/Email]

## 📝 Notes

- Migration script bảo toàn dữ liệu cũ bằng cách tạo 1 MaterialBatchItem cho mỗi MaterialBatch
- Các table cũ không dùng đến đã được xóa trong migration
- Breaking changes đã được document trong MATERIAL_BATCH_REFACTOR_SUMMARY.md
- API examples có trong API_EXAMPLES_MATERIAL_BATCH.md

## ⚠️ Risks & Mitigation

### Risk 1: Data Loss During Migration
**Mitigation**: 
- Backup database trước khi migrate
- Test migration trên dev/staging trước
- Có rollback plan

### Risk 2: Performance Issues
**Mitigation**:
- Add proper indexes
- Use entity graphs
- Monitor query performance
- Add caching if needed

### Risk 3: Breaking Changes
**Mitigation**:
- Version API if needed
- Maintain backward compatibility where possible
- Clear communication with team
- Gradual rollout

## 🎯 Success Criteria

- [ ] All existing functionality still works
- [ ] Can create batch with multiple materials
- [ ] Data migrated correctly without loss
- [ ] Performance acceptable (< 2s response time)
- [ ] No critical bugs in production
- [ ] User acceptance passed
- [ ] Documentation complete
