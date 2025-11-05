package ext.vnua.veterinary_beapp.modules.pcost.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.pcost.dto.GetProductionCostSheets;
import ext.vnua.veterinary_beapp.modules.pcost.dto.ProductionCostSheetDto;
import ext.vnua.veterinary_beapp.modules.pcost.dto.UpsertProductionCostSheetRequest;
import ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff;
import ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostItem;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import ext.vnua.veterinary_beapp.modules.pcost.repository.EnergyTariffRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.LaborRateRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.ProductionCostSheetRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.custom.CustomProductionCostSheetQuery;
import ext.vnua.veterinary_beapp.modules.pcost.service.ProductionCostSheetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor
public class ProductionCostSheetServiceImpl implements ProductionCostSheetService {

    private final ProductionCostSheetRepository repo;
    private final MaterialRepository materialRepo;
    private final LaborRateRepository laborRepo;
    private final EnergyTariffRepository energyRepo;

    // ========= CRUD =========

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ProductionCostSheet", description = "Tạo bảng chi phí sản xuất")
    public ProductionCostSheetDto create(UpsertProductionCostSheetRequest req) {
        repo.findBySheetCode(req.getSheetCode()).ifPresent(x -> {
            throw new DataExistException("Mã bảng chi phí đã tồn tại: " + req.getSheetCode());
        });
        ProductionCostSheet saved = repo.saveAndFlush(toEntity(new ProductionCostSheet(), req));
        return toDto(saved);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "ProductionCostSheet", description = "Cập nhật bảng chi phí sản xuất")
    public ProductionCostSheetDto update(Long id, UpsertProductionCostSheetRequest req) {
        ProductionCostSheet e = repo.findById(id)
                .orElseThrow(() -> new DataExistException("Bảng chi phí không tồn tại"));
        if (!e.getSheetCode().equals(req.getSheetCode())) {
            repo.findBySheetCode(req.getSheetCode()).ifPresent(x -> {
                throw new DataExistException("Mã bảng chi phí đã tồn tại: " + req.getSheetCode());
            });
        }
        e.getItems().clear();
        ProductionCostSheet saved = repo.saveAndFlush(toEntity(e, req));
        return toDto(saved);
    }

    @Override
    @Transactional
    public ProductionCostSheetDto get(Long id) {
        return toDto(repo.findById(id)
                .orElseThrow(() -> new DataExistException("Bảng chi phí không tồn tại")));
    }

    @Override
    @Transactional
    public ProductionCostSheetDto getByCode(String code) {
        return toDto(repo.findBySheetCode(code)
                .orElseThrow(() -> new DataExistException("Không tìm thấy mã bảng chi phí: " + code)));
    }

    @Override
    @Transactional
    public List<ProductionCostSheetDto> listByProduct(Long productId) {
        return repo.findByProductIdOrderByEffectiveDateDesc(productId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "ProductionCostSheet", description = "Xoá bảng chi phí sản xuất")
    public void delete(Long id) { repo.deleteById(id); }

    // ========= Mapping =========

    private ProductionCostSheet toEntity(ProductionCostSheet e, UpsertProductionCostSheetRequest r) {
        e.setProductId(r.getProductId());
        e.setSheetCode(r.getSheetCode().trim());
        e.setSheetName(r.getSheetName());
        e.setEffectiveDate(r.getEffectiveDate()!=null ? r.getEffectiveDate() : java.time.LocalDate.now());
        e.setSpecUnits(r.getSpecUnits());
        e.setIsActive(Boolean.TRUE.equals(r.getIsActive()));
        e.setNotes(r.getNotes());

        e.setTotalAmount(java.math.BigDecimal.ZERO);
        e.setUnitCost(java.math.BigDecimal.ZERO);

        // Don't replace the collection - add to existing one (already cleared in update method)
        if (r.getItems()!=null) {
            for (UpsertProductionCostSheetRequest.Item it : r.getItems()) {
                ProductionCostItem d = new ProductionCostItem();
                d.setSheet(e);
                d.setOrderNo(it.getOrderNo());
                d.setItemType(it.getItemType());
                d.setMaterialId(it.getMaterialId());
                d.setLaborRateId(it.getLaborRateId());
                d.setEnergyTariffId(it.getEnergyTariffId());
                d.setQuantity(it.getQuantity());

                d.setUnitPrice(java.math.BigDecimal.ZERO);
                d.setAmount(java.math.BigDecimal.ZERO);
                e.getItems().add(d);
            }
        }
        // totals sẽ tính ở toDto() theo "giá động"
        return e;
    }

    private ProductionCostSheetDto toDto(ProductionCostSheet e) {
        ProductionCostSheetDto d = new ProductionCostSheetDto();
        d.setId(e.getId());
        d.setProductId(e.getProductId());
        d.setSheetCode(e.getSheetCode());
        d.setSheetName(e.getSheetName());
        d.setEffectiveDate(e.getEffectiveDate());
        d.setSpecUnits(e.getSpecUnits());
        d.setIsActive(e.getIsActive());
        d.setNotes(e.getNotes());

        BigDecimal total = BigDecimal.ZERO;
        List<ProductionCostSheetDto.Item> its = new ArrayList<>();
        for (ProductionCostItem x : e.getItems()) {
            ProductionCostSheetDto.Item i = new ProductionCostSheetDto.Item();
            i.setId(x.getId());
            i.setOrderNo(x.getOrderNo());
            i.setQuantity(x.getQuantity());

            i.setItemType(x.getItemType());
            i.setMaterialId(x.getMaterialId());
            i.setLaborRateId(x.getLaborRateId());
            i.setEnergyTariffId(x.getEnergyTariffId());

            // Fill tên/mã/uom từ master + lấy đơn giá động
            BigDecimal unitPrice;
            switch (x.getItemType()) {
                case MATERIAL -> {
                    Material m = materialRepo.findById(x.getMaterialId())
                            .orElseThrow(() -> new DataExistException("Không thấy NVL: " + x.getMaterialId()));
                    i.setCostCode(m.getMaterialCode());
                    i.setCostName(m.getMaterialName());
                    var uom = m.getUnitOfMeasure();
                    String  uomName = uom != null ? uom.getName() : "N/A";
                    i.setUnitOfMeasure(uomName);
                    unitPrice = m.getFixedPrice()==null ? BigDecimal.ZERO : m.getFixedPrice();
                }
                case LABOR -> {
                    LaborRate l = laborRepo.findById(x.getLaborRateId())
                            .orElseThrow(() -> new DataExistException("Không thấy nhân công: " + x.getLaborRateId()));
                    i.setCostCode(l.getCode());
                    i.setCostName(l.getName());
                    // Get unit from UnitOfMeasure
                    String laborUomName = (l.getUnitOfMeasure() != null) ? l.getUnitOfMeasure().getName() : "giờ";
                    i.setUnitOfMeasure(laborUomName);
                    unitPrice = l.getPricePerUnit();
                }
                case ENERGY -> {
                    EnergyTariff t = energyRepo.findById(x.getEnergyTariffId())
                            .orElseThrow(() -> new DataExistException("Không thấy biểu giá điện: " + x.getEnergyTariffId()));
                    i.setCostCode(t.getCode());
                    i.setCostName(t.getName());
                    // Get unit from UnitOfMeasure
                    String energyUomName = (t.getUnitOfMeasure() != null) ? t.getUnitOfMeasure().getName() : "kWh";
                    i.setUnitOfMeasure(energyUomName);
                    unitPrice = t.getPricePerUnit();
                }
                default -> unitPrice = BigDecimal.ZERO;
            }

            unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
            i.setUnitPrice(unitPrice);
            BigDecimal amt = x.getQuantity().multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            i.setAmount(amt);
            total = total.add(amt);
            its.add(i);
        }

        d.setItems(its);
        d.setTotalAmount(total);
        int denom = (e.getSpecUnits()==null || e.getSpecUnits()<=0) ? 1 : e.getSpecUnits();
        d.setUnitCost(total.divide(BigDecimal.valueOf(denom), 2, RoundingMode.HALF_UP));
        return d;
    }


    @Override
    @Transactional
    public Page<ProductionCostSheetDto> search(GetProductionCostSheets req) {
        int pageIdx = Math.max(0, req.getStart());
        int pageSize = Math.max(1, req.getLimit());
        Pageable pageable = PageRequest.of(pageIdx, pageSize);

        var spec = CustomProductionCostSheetQuery.getFilter(req);
        Page<ProductionCostSheet> page = repo.findAll(spec, pageable);

        // map entity -> DTO (toDto() đã tính totalAmount, unitCost động)
        var dtos = page.getContent().stream().map(this::toDto).toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }
}
