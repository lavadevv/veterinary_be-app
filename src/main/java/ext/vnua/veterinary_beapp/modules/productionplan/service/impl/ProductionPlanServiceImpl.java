package ext.vnua.veterinary_beapp.modules.productionplan.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.material.model.Material;
import ext.vnua.veterinary_beapp.modules.material.model.MaterialBatchItem;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialBatchItemRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialRepository;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.MaterialBatchConsumptionDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.MaterialRequirementDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionCostMaterialDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionPlanFormulaContextDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDto;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CalculateMaterialRequirementRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CreateProductionLotRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.CreateProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.GetProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.ProductionPlanListRow;
import ext.vnua.veterinary_beapp.modules.productionplan.dto.request.UpdateProductionPlanRequest;
import ext.vnua.veterinary_beapp.modules.productionplan.enums.ProductionPlanStatus;
import ext.vnua.veterinary_beapp.modules.productionplan.mapper.ProductionPlanMapper;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionLot;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlan;
import ext.vnua.veterinary_beapp.modules.productionplan.model.ProductionPlanProduct;
import ext.vnua.veterinary_beapp.modules.productionplan.repository.ProductionLotRepository;
import ext.vnua.veterinary_beapp.modules.productionplan.repository.ProductionPlanRepository;
import ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionPlanQuery;
import ext.vnua.veterinary_beapp.modules.productionplan.service.ProductionPlanService;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostSheet;
import ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostItem;
import ext.vnua.veterinary_beapp.modules.pcost.repository.ProductionCostSheetRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.LaborRateRepository;
import ext.vnua.veterinary_beapp.modules.pcost.repository.EnergyTariffRepository;
import ext.vnua.veterinary_beapp.modules.product.model.FormulaHeader;
import ext.vnua.veterinary_beapp.modules.product.model.Product;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormula;
import ext.vnua.veterinary_beapp.modules.product.model.ProductFormulaItem;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductFormulaRepository;
import ext.vnua.veterinary_beapp.modules.product.repository.ProductRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionPlanServiceImpl implements ProductionPlanService {

    private static final int QUANTITY_SCALE = 3;
    private static final int MONEY_SCALE = 2;

    private final ProductionPlanRepository planRepository;
    private final ProductionLotRepository lotRepository;
    private final ProductFormulaRepository formulaRepository;
    private final ProductRepository productRepository;
    private final ProductionCostSheetRepository costSheetRepository;
    private final ProductionPlanMapper mapper;
    private final MaterialBatchItemRepository materialBatchItemRepository;
    private final MaterialRepository materialRepository;
    private final LaborRateRepository laborRateRepository;
    private final EnergyTariffRepository energyTariffRepository;

    // Removed single createPlan() — use createPlansBatch() instead

    @Override
    @Transactional
    public java.util.List<ProductionPlanDto> createPlansBatch(CreateProductionLotRequest request) {
        LocalDate planDate = request.getPlanDate() != null ? request.getPlanDate() : LocalDate.now();
        ProductionLot lot = createNewLot(planDate, request.getNotes());

        for (CreateProductionPlanRequest item : request.getPlans()) {
            ProductFormula formula = loadFormulaForPlanning(item.getFormulaId());
            PlanningContext ctx = buildContext(item.getBatchSize(), planDate);
            List<ProductionPlanProduct> lines = buildPlanLines(item.getProducts(), formula, ctx.batchSize());

            ProductionPlan plan = new ProductionPlan();
            plan.setLot(lot);
            plan.setFormula(formula);
            plan.setBatchSize(ctx.batchSize());
            plan.setNotes(item.getNotes());
            plan.setStatus(ProductionPlanStatus.PLANNING);
            lines.forEach(plan::addProductLine);
            lot.addPlan(plan);
        }

        ProductionLot saved = lotRepository.save(lot);
        return saved.getPlans().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public ProductionPlanDto getPlan(Long id) {
        ProductionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Production plan not found"));
        return mapper.toDto(plan);
    }

    @Override
    @Transactional
public Page<ProductionPlanDto> searchPlans(GetProductionPlanRequest request, Pageable pageable) {
        Specification<ProductionPlan> spec = CustomProductionPlanQuery.getFilter(toFilterParam(request));
        return planRepository.findAll(spec, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional
    public ProductionPlanDto updatePlan(Long id, UpdateProductionPlanRequest request) {
        ProductionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Production plan not found"));

        if (plan.getStatus() != ProductionPlanStatus.PLANNING) {
            throw new IllegalStateException("Only plans in PLANNING status can be updated");
        }

        ProductFormula formula = loadFormulaForPlanning(request.getFormulaId());
        PlanningContext context = buildContext(request.getBatchSize(), request.getPlanDate());
        List<ProductionPlanProduct> lines = buildPlanLines(request.getProducts(), formula, context.batchSize());

        // Keep same lot for plan or create a new one
        ProductionLot lot = plan.getLot();
        if (lot == null) {
            lot = createNewLot(context.planDate(), request.getNotes());
            plan.setLot(lot);
        } else {
            lot.setPlanDate(context.planDate());
            lot.setPlanMonth(context.planDate().getMonthValue());
            lot.setPlanYear(context.planDate().getYear());
        }
        plan.setFormula(formula);
        plan.setBatchSize(context.batchSize());
        plan.setNotes(request.getNotes());

        plan.getProductLines().clear();
        lines.forEach(plan::addProductLine);

        ProductionPlan savedPlan = planRepository.save(plan);
        return mapper.toDto(savedPlan);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        ProductionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Production plan not found"));

        if (plan.getStatus() != ProductionPlanStatus.PLANNING) {
            throw new IllegalStateException("Only plans in PLANNING status can be deleted");
        }

        planRepository.delete(plan);
    }

    @Override
    @Transactional
    public ProductionPlanFormulaContextDto getFormulaContext(Long formulaId) {
        ProductFormula formula = loadFormula(formulaId);

        ProductionPlanFormulaContextDto dto = new ProductionPlanFormulaContextDto();
        dto.setFormulaId(formula.getId());
        dto.setVersion(formula.getVersion());
        dto.setDefaultBatchSize(formula.getBatchSize());
        dto.setActive(formula.getIsActive());

        if (formula.getHeader() != null) {
            dto.setFormulaCode(formula.getHeader().getFormulaCode());
            dto.setFormulaName(formula.getHeader().getFormulaName());

            formula.getHeader().getProducts().forEach(product -> {
                ProductionPlanFormulaContextDto.ProductInfo info = new ProductionPlanFormulaContextDto.ProductInfo();
                info.setProductId(product.getId());
                info.setProductCode(product.getProductCode());
                info.setProductName(product.getProductName());
                info.setUnitOfMeasure(product.getUnitOfMeasure());
                info.setActive(product.getIsActive());
                dto.getProducts().add(info);
            });
        }

        return dto;
    }

    private ProductFormula loadFormula(Long formulaId) {
        ProductFormula formula = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new DataExistException("Production formula does not exist"));

        if (formula.getHeader() != null && formula.getHeader().getProducts() != null) {
            formula.getHeader().getProducts().size();
        }
        return formula;
    }

    private ProductFormula loadFormulaByCode(String formulaCode) {
        ProductFormula formula = formulaRepository.findByHeaderFormulaCodeAndIsActiveTrue(formulaCode)
                .orElseThrow(() -> new DataExistException("Production formula with code '" + formulaCode + "' does not exist or is inactive"));

        if (formula.getHeader() != null && formula.getHeader().getProducts() != null) {
            formula.getHeader().getProducts().size();
        }
        return formula;
    }

    private ProductFormula loadFormulaForPlanning(Long formulaId) {
        ProductFormula formula = loadFormula(formulaId);
        if (Boolean.FALSE.equals(formula.getIsActive())) {
            throw new IllegalStateException("Cannot use an inactive formula for production planning");
        }
        return formula;
    }

    private List<ProductionPlanProduct> buildPlanLines(List<CreateProductionPlanRequest.ProductLine> items,
                                                       ProductFormula formula,
                                                       BigDecimal batchSize) {
        if (CollectionUtils.isEmpty(items)) {
            throw new IllegalArgumentException("Products to produce must not be empty");
        }

        List<Long> productIds = items.stream()
                .map(CreateProductionPlanRequest.ProductLine::getProductId)
                .distinct()
                .toList();

        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        if (productMap.size() != productIds.size()) {
            throw new DataExistException("Invalid product id in request");
        }

        Set<Long> allowedProductIds = formula.getHeader() != null
                ? formula.getHeader().getProducts().stream().map(Product::getId).collect(Collectors.toSet())
                : Collections.emptySet();

        Set<Long> costSheetIds = items.stream()
                .map(CreateProductionPlanRequest.ProductLine::getProductionCostSheetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ProductionCostSheet> costSheetMap = costSheetIds.isEmpty()
                ? Collections.emptyMap()
                : costSheetRepository.findAllById(costSheetIds).stream()
                        .collect(Collectors.toMap(ProductionCostSheet::getId, c -> c));

        if (costSheetMap.size() != costSheetIds.size()) {
            throw new DataExistException("One or more production cost sheets were not found");
        }

        BigDecimal sumQuantity = BigDecimal.ZERO;
        List<ProductionPlanProduct> result = new ArrayList<>();

        for (CreateProductionPlanRequest.ProductLine line : items) {
            Product product = productMap.get(line.getProductId());
            if (product == null) {
                throw new DataExistException("Product not found: id=" + line.getProductId());
            }

            if (!allowedProductIds.isEmpty() && !allowedProductIds.contains(product.getId())) {
                throw new IllegalStateException("Product " + product.getProductCode() + " is not linked to the selected formula");
            }

            BigDecimal plannedQty = normalizeQuantity(line.getPlannedQuantity());
            sumQuantity = sumQuantity.add(plannedQty);

            ProductionPlanProduct entity = new ProductionPlanProduct();
            entity.setProduct(product);
            entity.setProductBrand(line.getProductBrand()); // Set brand from request
            entity.setUnitOfMeasure(product.getUnitOfMeasure());
            entity.setPlannedQuantity(plannedQty);
            entity.setNotes(line.getNotes());

            if (line.getActualQuantity() != null) {
                entity.setActualQuantity(normalizeQuantity(line.getActualQuantity()));
            }

            ProductionCostSheet costSheet = line.getProductionCostSheetId() != null
                    ? costSheetMap.get(line.getProductionCostSheetId())
                    : null;

            BigDecimal plannedUnitCost = resolvePlannedUnitCost(line.getPlannedUnitCost(), costSheet);
            entity.setPlannedUnitCost(plannedUnitCost);

            if (plannedUnitCost != null) {
                BigDecimal totalCost = plannedUnitCost.multiply(plannedQty).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                entity.setPlannedTotalCost(totalCost);
            } else {
                entity.setPlannedTotalCost(null);
            }

            if (costSheet != null) {
                entity.setProductionCostSheet(costSheet);
                entity.setProductionCostSheetCode(costSheet.getSheetCode());
                entity.setProductionCostSheetName(costSheet.getSheetName());
                entity.setProductionCostSpecUnits(costSheet.getSpecUnits());
            } else {
                entity.setProductionCostSheet(null);
                entity.setProductionCostSheetCode(null);
                entity.setProductionCostSheetName(null);
                entity.setProductionCostSpecUnits(null);
            }

            result.add(entity);
        }

        if (sumQuantity.compareTo(batchSize) != 0) {
            throw new IllegalStateException(String.format(
                    "Total planned quantity (%s) does not match batch size (%s). Please adjust.",
                    sumQuantity.stripTrailingZeros().toPlainString(),
                    batchSize.stripTrailingZeros().toPlainString()
            ));
        }

        return result;
    }

    private CustomProductionPlanQuery.ProductionPlanFilterParam toFilterParam(GetProductionPlanRequest req) {
        var p = new CustomProductionPlanQuery.ProductionPlanFilterParam();
        if (req == null) return p;
        p.setLotNumber(req.getLotNumber());
        p.setKeywords(req.getKeywords());
        p.setFormulaId(req.getFormulaId());
        p.setProductId(req.getProductId());
        p.setStatus(req.getStatus());
        p.setFromDate(req.getFromDate());
        p.setToDate(req.getToDate());
        return p;
    }

    @Override
    @Transactional
    public Page<ProductionPlanListRow> getAllPlanRows(CustomProductionPlanQuery.ProductionPlanFilterParam param, PageRequest pageRequest) {
        Specification<ProductionPlan> spec = CustomProductionPlanQuery.getFilter(param);
        Page<ProductionPlan> page = planRepository.findAll(spec, pageRequest);

        var rows = page.getContent().stream().map(p -> {
            long lineCount = (p.getProductLines() == null) ? 0 : p.getProductLines().size();
            BigDecimal totalPlanned = (p.getProductLines() == null) ? BigDecimal.ZERO :
                    p.getProductLines().stream()
                            .map(ProductionPlanProduct::getPlannedQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new ProductionPlanListRow(
                    p.getId(),
                    (p.getLot()!=null ? p.getLot().getLotNumber() : null),
                    (p.getLot()!=null ? p.getLot().getPlanDate() : null),
                    (p.getLot()!=null ? p.getLot().getPlanMonth() : null),
                    (p.getLot()!=null ? p.getLot().getPlanYear() : null),
                    p.getBatchSize(),
                    p.getStatus(),
                    p.getFormula() != null ? p.getFormula().getId() : null,
                    (p.getFormula() != null && p.getFormula().getHeader()!=null) ? p.getFormula().getHeader().getFormulaCode() : null,
                    (p.getFormula() != null && p.getFormula().getHeader()!=null) ? p.getFormula().getHeader().getFormulaName() : null,
                    p.getFormula() != null ? p.getFormula().getVersion() : null,
                    lineCount,
                    totalPlanned,
                    p.getCreatedDate(),
                    p.getCreatedBy()
            );
        }).toList();

        return new org.springframework.data.domain.PageImpl<>(rows, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional
    public Page<ProductionLotDto> searchLots(ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionLotQuery.ProductionLotFilterParam param, PageRequest pageRequest) {
        var spec = ext.vnua.veterinary_beapp.modules.productionplan.repository.custom.CustomProductionLotQuery.getFilter(param);
        var page = lotRepository.findAll(spec, pageRequest);
        var rows = page.getContent().stream().map(this::toLotDto).toList();
        return new org.springframework.data.domain.PageImpl<>(rows, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional
    public ProductionLotDto getLot(Long id) {
        ProductionLot lot = lotRepository.findByIdWithPlansAndFormulas(id)
                .orElseThrow(() -> new DataExistException("Production lot not found"));
        return toLotDto(lot);
    }

    @Override
    @Transactional
    public ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDetailDto getLotDetail(Long id) {
        // Step 1: Fetch lot with plans and formulas (avoiding MultipleBagFetchException)
        ProductionLot lot = lotRepository.findByIdWithPlansAndFormulas(id)
                .orElseThrow(() -> new DataExistException("Production lot not found"));
        
        // Step 2: Fetch product lines separately to avoid multiple bag fetch
        java.util.List<ProductionPlan> plansWithLines = lotRepository.findPlansByLotIdWithProductLines(id);
        
        // Map plans by ID for quick lookup
        java.util.Map<Long, ProductionPlan> planMap = new java.util.HashMap<>();
        for (ProductionPlan p : plansWithLines) {
            planMap.put(p.getId(), p);
        }
        
        ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDetailDto dto = 
                new ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDetailDto();
        
        // Copy lot info
        dto.setId(lot.getId());
        dto.setLotNumber(lot.getLotNumber());
        dto.setSequenceInMonth(lot.getSequenceInMonth());
        dto.setPlanMonth(lot.getPlanMonth());
        dto.setPlanYear(lot.getPlanYear());
        dto.setPlanDate(lot.getPlanDate());
        dto.setStatus(lot.getStatus());
        dto.setNotes(lot.getNotes());
        dto.setCreatedDate(lot.getCreatedDate());
        dto.setLastModifiedDate(lot.getLastModifiedDate());
        dto.setCreatedBy(lot.getCreatedBy());
        dto.setLastModifiedBy(lot.getLastModifiedBy());
        
        // Build product rows from all plans
        if (lot.getPlans() != null) {
            int batchNumber = 0;
            for (ProductionPlan plan : lot.getPlans()) {
                batchNumber++;
                
                String formulaCode = null;
                String formulaName = null;
                if (plan.getFormula() != null && plan.getFormula().getHeader() != null) {
                    formulaCode = plan.getFormula().getHeader().getFormulaCode();
                    formulaName = plan.getFormula().getHeader().getFormulaName();
                }
                
                // Get the plan with product lines from the second query
                ProductionPlan planWithLines = planMap.get(plan.getId());
                if (planWithLines != null && planWithLines.getProductLines() != null) {
                    for (ProductionPlanProduct productLine : planWithLines.getProductLines()) {
                        ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDetailDto.PlanProductRow row = 
                                new ext.vnua.veterinary_beapp.modules.productionplan.dto.ProductionLotDetailDto.PlanProductRow();
                        
                        // Plan info
                        row.setPlanId(plan.getId());
                        row.setFormulaCode(formulaCode);
                        row.setFormulaName(formulaName);
                        row.setBatchSize(plan.getBatchSize());
                        row.setBatchNumber(batchNumber);
                        
                        // Product line info
                        row.setProductLineId(productLine.getId());
                        if (productLine.getProduct() != null) {
                            row.setProductId(productLine.getProduct().getId());
                            row.setProductCode(productLine.getProduct().getProductCode());
                            row.setProductName(productLine.getProduct().getProductName());
                        }
                        row.setUnitOfMeasure(productLine.getUnitOfMeasure());
                        
                        // Production cost info
                        row.setProductionCostSheetId(
                            productLine.getProductionCostSheet() != null 
                                ? productLine.getProductionCostSheet().getId() 
                                : null
                        );
                        row.setProductionCostSheetCode(productLine.getProductionCostSheetCode());
                        row.setProductionCostSheetName(productLine.getProductionCostSheetName());
                        row.setSpecUnits(productLine.getProductionCostSpecUnits());
                        
                        // Brand
                        row.setProductBrand(productLine.getProductBrand());
                        
                        // Quantities
                        row.setPlannedQuantity(productLine.getPlannedQuantity());
                        row.setActualQuantity(productLine.getActualQuantity());
                        
                        // Cost
                        row.setPlannedUnitCost(productLine.getPlannedUnitCost());
                        row.setPlannedTotalCost(productLine.getPlannedTotalCost());
                        
                        dto.getProducts().add(row);
                    }
                }
            }
        }
        
        return dto;
    }

    private ProductionLotDto toLotDto(ProductionLot lot) {
        ProductionLotDto dto = new ProductionLotDto();
        dto.setId(lot.getId());
        dto.setLotNumber(lot.getLotNumber());
        dto.setSequenceInMonth(lot.getSequenceInMonth());
        dto.setPlanMonth(lot.getPlanMonth());
        dto.setPlanYear(lot.getPlanYear());
        dto.setPlanDate(lot.getPlanDate());
        dto.setStatus(lot.getStatus());
        dto.setNotes(lot.getNotes());
        dto.setCreatedDate(lot.getCreatedDate());
        dto.setLastModifiedDate(lot.getLastModifiedDate());
        dto.setCreatedBy(lot.getCreatedBy());
        dto.setLastModifiedBy(lot.getLastModifiedBy());

        int planCount = lot.getPlans() == null ? 0 : lot.getPlans().size();
        dto.setPlanCount(planCount);

        // Calculate total using separate query to avoid lazy loading issues
        java.math.BigDecimal total = calculateLotTotalPlannedQty(lot.getId());
        dto.setTotalPlannedQty(total);
        
        // Build formula list from plans (formulas are already fetched)
        if (lot.getPlans() != null) {
            for (var plan : lot.getPlans()) {
                if (plan.getFormula() != null) {
                    var fb = new ProductionLotDto.FormulaBrief();
                    fb.setFormulaId(plan.getFormula().getId());
                    if (plan.getFormula().getHeader() != null) {
                        fb.setFormulaCode(plan.getFormula().getHeader().getFormulaCode());
                        fb.setFormulaName(plan.getFormula().getHeader().getFormulaName());
                    }
                    fb.setVersion(plan.getFormula().getVersion());
                    boolean exists = dto.getFormulas().stream()
                            .anyMatch(x -> x.getFormulaId() != null && x.getFormulaId().equals(fb.getFormulaId()));
                    if (!exists) dto.getFormulas().add(fb);
                }
            }
        }
        return dto;
    }
    
    /**
     * Calculate total planned quantity for a lot using a separate query
     * to avoid lazy loading issues
     */
    private java.math.BigDecimal calculateLotTotalPlannedQty(Long lotId) {
        // Use native query or JPQL to sum directly from database
        java.math.BigDecimal total = planRepository.sumPlannedQuantityByLotId(lotId);
        return total != null ? total : java.math.BigDecimal.ZERO;
    }

    private ProductionLot createNewLot(LocalDate planDate, String notes) {
        int year = planDate.getYear();
        int month = planDate.getMonthValue();

        int nextSequence = lotRepository.findMaxSequenceInMonth(year, month) + 1;
        if (nextSequence > 99) {
            throw new IllegalStateException("Monthly lot number limit (99) exceeded. Please review existing plans.");
        }

        ProductionLot lot = new ProductionLot();
        lot.setPlanDate(planDate);
        lot.setPlanMonth(month);
        lot.setPlanYear(year);
        lot.setSequenceInMonth(nextSequence);
        lot.setLotNumber(String.format("%02d%02d%02d", nextSequence, month, year % 100));
        lot.setStatus(ProductionPlanStatus.PLANNING);
        lot.setNotes(notes);
        return lot;
    }

    private BigDecimal resolvePlannedUnitCost(BigDecimal override, ProductionCostSheet sheet) {
        if (override != null) {
            return override.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        if (sheet != null && sheet.getUnitCost() != null) {
            return sheet.getUnitCost().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return null;
    }

    private BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
    }

    private PlanningContext buildContext(BigDecimal batchSize, LocalDate planDate) {
        if (batchSize == null) {
            throw new IllegalArgumentException("Batch size is required");
        }
        BigDecimal normalizedBatch = normalizeQuantity(batchSize);
        LocalDate effectiveDate = planDate != null ? planDate : LocalDate.now();
        return new PlanningContext(normalizedBatch, effectiveDate);
    }

    @Override
    @Transactional
    public MaterialRequirementDto calculateMaterialRequirements(CalculateMaterialRequirementRequest request) {
        // Load formula with formula items
        ProductFormula formula = loadFormula(request.getFormulaId());
        
        if (CollectionUtils.isEmpty(formula.getFormulaItems())) {
            throw new IllegalStateException("Formula has no material items");
        }

        MaterialRequirementDto result = new MaterialRequirementDto();
        
        // Set formula info
        result.setFormulaId(formula.getId());
        result.setFormulaVersion(formula.getVersion());
        if (formula.getHeader() != null) {
            result.setFormulaCode(formula.getHeader().getFormulaCode());
            result.setFormulaName(formula.getHeader().getFormulaName());
        }
        
        // Set batch info
        BigDecimal batchSize = request.getBatchSize();
        String batchUnit = request.getBatchUnit();
        if (batchUnit == null || batchUnit.isEmpty()) {
            batchUnit = formula.getBasisUnit() != null ? formula.getBasisUnit() : "g";
        }
        result.setBatchSize(batchSize);
        result.setBatchUnit(batchUnit);
        
        // Get basis value and unit for calculation
        BigDecimal basisValue = formula.getBasisValue() != null ? formula.getBasisValue() : BigDecimal.valueOf(1000);
        String basisUnit = formula.getBasisUnit() != null ? formula.getBasisUnit() : "g";
        
        // Convert batchSize to same unit as basisValue for accurate calculation
        BigDecimal batchSizeInBasisUnit = convertToUnit(batchSize, batchUnit, basisUnit);
        
        // Calculate scaling factor: actualBatchSize / basisValue
        BigDecimal scalingFactor = batchSizeInBasisUnit.divide(basisValue, 6, RoundingMode.HALF_UP);
        
        BigDecimal totalQuantityG = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        int orderNo = 1;
        
        // Calculate for each material
        for (ProductFormulaItem item : formula.getFormulaItems()) {
            Material material = item.getMaterial();
            if (material == null) continue;
            
            MaterialRequirementDto.MaterialItem materialItem = new MaterialRequirementDto.MaterialItem();
            materialItem.setOrderNo(orderNo++);
            materialItem.setMaterialId(material.getId());
            materialItem.setMaterialCode(material.getMaterialCode());
            materialItem.setMaterialName(material.getMaterialName());
            
            // Get material type from category if available
            if (material.getMaterialCategory() != null) {
                materialItem.setMaterialType(material.getMaterialCategory().getCategoryName());
            }
            
            // Get percentage from formula item
            BigDecimal percentage = item.getPercentage() != null ? item.getPercentage() : BigDecimal.ZERO;
            materialItem.setPercentage(percentage);
            
            // Calculate required quantity
            // Formula: (percentage / 100) × scalingFactor × basisValue
            // Example: 5% material, 100kg batch (100,000g), basisValue=1000g
            // → scalingFactor = 100,000/1000 = 100
            // → required = 0.05 × 100 × 1000 = 5,000g
            BigDecimal requiredQuantityInBasisUnit = percentage
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                    .multiply(scalingFactor)
                    .multiply(basisValue)
                    .setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
            
            materialItem.setRequiredQuantity(requiredQuantityInBasisUnit);
            materialItem.setUnit(basisUnit);
            
            // Get unit price (VNĐ/g) - use fixedPrice
            BigDecimal unitPrice = material.getFixedPrice() != null ? material.getFixedPrice() : BigDecimal.ZERO;
            
            // Convert unit price to per gram if needed
            String materialUom = material.getUnitOfMeasure() != null 
                    ? material.getUnitOfMeasure().getName()
                    : "g";
            if ("kg".equalsIgnoreCase(materialUom)) {
                unitPrice = unitPrice.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            }
            materialItem.setUnitPrice(unitPrice);
            
            // Calculate amount
            BigDecimal amount = requiredQuantityInBasisUnit
                    .multiply(unitPrice)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            materialItem.setAmount(amount);
            
            materialItem.setNotes(item.getNotes());
            materialItem.setIsCritical(item.getIsCritical());
            
            result.getMaterials().add(materialItem);
            
            totalQuantityG = totalQuantityG.add(requiredQuantityInBasisUnit);
            totalAmount = totalAmount.add(amount);
        }
        
        // Set summary
        MaterialRequirementDto.Summary summary = new MaterialRequirementDto.Summary();
        summary.setTotalQuantityKg(totalQuantityG.divide(BigDecimal.valueOf(1000), QUANTITY_SCALE, RoundingMode.HALF_UP));
        summary.setTotalAmount(totalAmount);
        summary.setMaterialCount(result.getMaterials().size());
        result.setSummary(summary);
        
        return result;
    }

    @Override
    @Transactional
    public MaterialBatchConsumptionDto simulateMaterialConsumption(
            ext.vnua.veterinary_beapp.modules.productionplan.dto.request.SimulateMaterialConsumptionRequest request
    ) {
        // Load formula - support both formulaId and formulaCode
        ProductFormula formula;
        if (request.getFormulaId() != null) {
            formula = loadFormula(request.getFormulaId());
        } else if (request.getFormulaCode() != null && !request.getFormulaCode().isEmpty()) {
            formula = loadFormulaByCode(request.getFormulaCode());
        } else {
            throw new IllegalArgumentException("Either formulaId or formulaCode must be provided");
        }
        
        if (CollectionUtils.isEmpty(formula.getFormulaItems())) {
            throw new IllegalStateException("Formula has no material items");
        }

        MaterialBatchConsumptionDto result = new MaterialBatchConsumptionDto();
        
        // Set formula info
        result.setFormulaId(formula.getId());
        result.setFormulaVersion(formula.getVersion());
        if (formula.getHeader() != null) {
            result.setFormulaCode(formula.getHeader().getFormulaCode());
            result.setFormulaName(formula.getHeader().getFormulaName());
        }
        
        // Set batch info
        BigDecimal batchSize = request.getBatchSize();
        String batchUnit = request.getBatchUnit();
        if (batchUnit == null || batchUnit.isEmpty()) {
            batchUnit = formula.getBasisUnit() != null ? formula.getBasisUnit() : "g";
        }
        result.setBatchSize(batchSize);
        result.setBatchUnit(batchUnit);
        
        // Set lot info if provided
        if (request.getLotId() != null) {
            ProductionLot lot = lotRepository.findById(request.getLotId())
                    .orElseThrow(() -> new DataExistException("Lot not found"));
            result.setLotId(lot.getId());
            result.setLotNumber(lot.getLotNumber());
        }
        
        // Get basis value and unit for calculation
        BigDecimal basisValue = formula.getBasisValue() != null ? formula.getBasisValue() : BigDecimal.valueOf(1000);
        String basisUnit = formula.getBasisUnit() != null ? formula.getBasisUnit() : "g";
        
        // Convert batchSize to same unit as basisValue for accurate calculation
        BigDecimal batchSizeInBasisUnit = convertToUnit(batchSize, batchUnit, basisUnit);
        
        // Calculate scaling factor: actualBatchSize / basisValue
        // Example: 100 kg = 100,000 g. If basisValue = 1000g, then scalingFactor = 100
        BigDecimal scalingFactor = batchSizeInBasisUnit.divide(basisValue, 6, RoundingMode.HALF_UP);
        
        int sufficientCount = 0;
        int shortageCount = 0;
        BigDecimal totalEstimatedCost = BigDecimal.ZERO;
        
        // Process each material
        for (ProductFormulaItem item : formula.getFormulaItems()) {
            Material material = item.getMaterial();
            if (material == null) continue;
            
            MaterialBatchConsumptionDto.MaterialConsumption consumption = 
                    new MaterialBatchConsumptionDto.MaterialConsumption();
            
            consumption.setMaterialId(material.getId());
            consumption.setMaterialCode(material.getMaterialCode());
            consumption.setMaterialName(material.getMaterialName());
            
            if (material.getMaterialCategory() != null) {
                consumption.setMaterialType(material.getMaterialCategory().getCategoryName());
            }
            
            // Calculate required quantity
            // Formula: (percentage / 100) × scalingFactor
            // scalingFactor already includes the batch size conversion
            BigDecimal percentage = item.getPercentage() != null ? item.getPercentage() : BigDecimal.ZERO;
            BigDecimal requiredQuantityInBasisUnit = percentage
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                    .multiply(scalingFactor)
                    .multiply(basisValue) // percentage of scaled batch
                    .setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
            
            consumption.setRequiredQuantity(requiredQuantityInBasisUnit);
            consumption.setUnit(basisUnit);
            
            // Find available MaterialBatchItems using FIFO
            List<MaterialBatchItem> availableItems = materialBatchItemRepository
                    .findFIFOItemsForAllocation(material.getId());
            
            BigDecimal remainingNeed = requiredQuantityInBasisUnit;
            
            // Pick from batches using FIFO
            for (MaterialBatchItem batchItem : availableItems) {
                if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) break;
                
                BigDecimal availableQty = batchItem.getAvailableQuantity();
                if (availableQty == null || availableQty.compareTo(BigDecimal.ZERO) <= 0) continue;
                
                BigDecimal pickQty = remainingNeed.min(availableQty);
                BigDecimal remainingAfterPick = availableQty.subtract(pickQty);
                
                MaterialBatchConsumptionDto.BatchPick pick = new MaterialBatchConsumptionDto.BatchPick();
                pick.setMaterialBatchId(batchItem.getId());
                pick.setBatchNumber(batchItem.getBatch() != null ? batchItem.getBatch().getBatchNumber() : "N/A");
                pick.setManufacturingDate(batchItem.getManufacturingDate());
                pick.setExpiryDate(batchItem.getExpiryDate());
                pick.setCurrentStock(availableQty);
                pick.setPickQuantity(pickQty);
                pick.setRemainingStock(remainingAfterPick);
                pick.setUnit(basisUnit);
                
                // Calculate cost
                BigDecimal unitCost = batchItem.getUnitPrice() != null ? batchItem.getUnitPrice() : BigDecimal.ZERO;
                pick.setUnitCost(unitCost);
                
                BigDecimal amount = pickQty.multiply(unitCost).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                pick.setAmount(amount);
                
                consumption.getBatchPicks().add(pick);
                remainingNeed = remainingNeed.subtract(pickQty);
                totalEstimatedCost = totalEstimatedCost.add(amount);
            }
            
            // Check if sufficient
            if (remainingNeed.compareTo(BigDecimal.ZERO) > 0) {
                consumption.setShortageQuantity(remainingNeed);
                consumption.setIsSufficient(false);
                shortageCount++;
            } else {
                consumption.setShortageQuantity(BigDecimal.ZERO);
                consumption.setIsSufficient(true);
                sufficientCount++;
            }
            
            result.getMaterials().add(consumption);
        }
        
        // Set summary
        MaterialBatchConsumptionDto.ConsumptionSummary summary = new MaterialBatchConsumptionDto.ConsumptionSummary();
        summary.setMaterialCount(result.getMaterials().size());
        summary.setSufficientCount(sufficientCount);
        summary.setShortageCount(shortageCount);
        summary.setTotalEstimatedCost(totalEstimatedCost);
        summary.setCanExecute(shortageCount == 0);
        result.setSummary(summary);
        
        return result;
    }

    @Override
    @Transactional
    public ProductionCostMaterialDto getProductionCostMaterials(Long lotId) {
        // Load lot with plans and formulas (avoid lazy init issues on plans)
        ProductionLot lot = lotRepository.findByIdWithPlansAndFormulas(lotId)
                .orElseThrow(() -> new DataExistException("Production lot not found"));

        // Load plans with product lines in a separate fetch to avoid MultipleBagFetchException
        java.util.List<ProductionPlan> plansWithLines = lotRepository.findPlansByLotIdWithProductLines(lotId);
        if (plansWithLines == null || plansWithLines.isEmpty()) {
            throw new IllegalStateException("Lot has no production plans");
        }
        
        ProductionCostMaterialDto result = new ProductionCostMaterialDto();
        result.setLotId(lot.getId());
        result.setLotNumber(lot.getLotNumber());
        
        // Get formula info from first product (for header display)
        ProductionPlan firstPlan = plansWithLines.get(0);
        if (firstPlan.getFormula() != null && firstPlan.getFormula().getHeader() != null) {
            result.setFormulaCode(firstPlan.getFormula().getHeader().getFormulaCode());
            result.setFormulaName(firstPlan.getFormula().getHeader().getFormulaName());
        }
        
        // Calculate total batch size from all plans
        BigDecimal totalBatchSize = plansWithLines.stream()
                .map(ProductionPlan::getBatchSize)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.setTotalBatchSize(totalBatchSize);
        result.setBatchUnit("kg"); // Default unit
        
        // Collect all cost sheet IDs to eager load
        Set<Long> costSheetIds = new HashSet<>();
        Set<Long> materialIds = new HashSet<>();
        Set<Long> laborRateIds = new HashSet<>();
        Set<Long> energyTariffIds = new HashSet<>();
        
        // First pass: collect all products and their cost sheets
        List<ProductWithCostSheet> productsWithCostSheets = new ArrayList<>();
        
        for (ProductionPlan plan : plansWithLines) {
            if (plan.getProductLines() == null) continue;
            
            for (ProductionPlanProduct planProduct : plan.getProductLines()) {
                if (planProduct.getProductionCostSheet() == null) {
                    log.warn("Product has no production cost sheet");
                    continue;
                }
                
                costSheetIds.add(planProduct.getProductionCostSheet().getId());
                productsWithCostSheets.add(new ProductWithCostSheet(planProduct, planProduct.getProductionCostSheet()));
            }
        }
        
        // Load all cost sheets with items in one query
        Map<Long, ProductionCostSheet> costSheetMap = new HashMap<>();
        if (!costSheetIds.isEmpty()) {
            List<ProductionCostSheet> costSheets = costSheetRepository.findAllById(costSheetIds);
            for (ProductionCostSheet sheet : costSheets) {
                costSheetMap.put(sheet.getId(), sheet);
                
                // Collect master IDs from items
                if (sheet.getItems() != null) {
                    for (ext.vnua.veterinary_beapp.modules.pcost.model.ProductionCostItem item : sheet.getItems()) {
                        if (item.getMaterialId() != null) {
                            materialIds.add(item.getMaterialId());
                        }
                        if (item.getLaborRateId() != null) {
                            laborRateIds.add(item.getLaborRateId());
                        }
                        if (item.getEnergyTariffId() != null) {
                            energyTariffIds.add(item.getEnergyTariffId());
                        }
                    }
                }
            }
        }
        
        // Load all masters in batch
        Map<Long, Material> materialMap = new HashMap<>();
        if (!materialIds.isEmpty()) {
            materialRepository.findAllById(materialIds).forEach(m -> materialMap.put(m.getId(), m));
        }
        
        Map<Long, ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate> laborRateMap = new HashMap<>();
        if (!laborRateIds.isEmpty()) {
            laborRateRepository.findAllById(laborRateIds).forEach(lr -> laborRateMap.put(lr.getId(), lr));
        }
        
        Map<Long, ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff> energyTariffMap = new HashMap<>();
        if (!energyTariffIds.isEmpty()) {
            energyTariffRepository.findAllById(energyTariffIds).forEach(et -> energyTariffMap.put(et.getId(), et));
        }
        
        // Process each product
        int totalItems = 0;
        int materialItemCount = 0;
        int laborItemCount = 0;
        int energyItemCount = 0;
        
        for (ProductWithCostSheet pwc : productsWithCostSheets) {
            ProductionPlanProduct planProduct = pwc.planProduct;
            ProductionCostSheet costSheet = costSheetMap.get(pwc.costSheet.getId());
            
            if (costSheet == null) {
                log.warn("Cost sheet not found for product");
                continue;
            }
            
            ProductionCostMaterialDto.ProductCostDetail detail = new ProductionCostMaterialDto.ProductCostDetail();
            
            // Get product info from Product entity
            if (planProduct.getProduct() != null) {
                detail.setProductId(planProduct.getProduct().getId());
                detail.setProductCode(planProduct.getProduct().getProductCode());
                detail.setProductName(planProduct.getProduct().getProductName());
            }
            detail.setPlannedQuantity(planProduct.getPlannedQuantity());
            detail.setUnitOfMeasure(planProduct.getUnitOfMeasure());
            
            // Cost sheet info
            ProductionCostMaterialDto.CostSheetInfo sheetInfo = new ProductionCostMaterialDto.CostSheetInfo();
            sheetInfo.setId(costSheet.getId());
            sheetInfo.setSheetCode(costSheet.getSheetCode());
            sheetInfo.setSheetName(costSheet.getSheetName());
            sheetInfo.setSpecUnits(costSheet.getSpecUnits());
            detail.setCostSheet(sheetInfo);
            
            // Calculate scale factor
            BigDecimal plannedQty = planProduct.getPlannedQuantity() != null ? planProduct.getPlannedQuantity() : BigDecimal.ONE;
            Integer specUnits = costSheet.getSpecUnits() != null && costSheet.getSpecUnits() > 0 ? costSheet.getSpecUnits() : 1;
            BigDecimal scaleFactor = plannedQty.divide(BigDecimal.valueOf(specUnits), 6, RoundingMode.HALF_UP);
            
            // Process cost items
            if (costSheet.getItems() != null) {
                for (ProductionCostItem item : costSheet.getItems()) {
                    ProductionCostMaterialDto.CostMaterialItem materialItem = new ProductionCostMaterialDto.CostMaterialItem();
                    
                    materialItem.setOrderNo(item.getOrderNo());
                    materialItem.setItemType(item.getItemType() != null ? item.getItemType().name() : "UNKNOWN");
                    
                    // Get item code and name based on type
                    String itemCode = "N/A";
                    String itemName = "Unknown";
                    String unit = item.getUnitOfMeasure();
                    
                    switch (item.getItemType()) {
                        case MATERIAL:
                            if (item.getMaterialId() != null) {
                                Material material = materialMap.get(item.getMaterialId());
                                if (material != null) {
                                    itemCode = material.getMaterialCode();
                                    itemName = material.getMaterialName();
                                    if (material.getUnitOfMeasure() != null) {
                                        unit = material.getUnitOfMeasure().getName();
                                    }
                                }
                            }
                            materialItemCount++;
                            break;
                            
                        case LABOR:
                            if (item.getLaborRateId() != null) {
                                ext.vnua.veterinary_beapp.modules.pcost.model.LaborRate laborRate = laborRateMap.get(item.getLaborRateId());
                                if (laborRate != null) {
                                    itemCode = laborRate.getCode();
                                    itemName = laborRate.getName();
                                    if (laborRate.getUnitOfMeasure() != null) {
                                        unit = laborRate.getUnitOfMeasure().getName();
                                    }
                                }
                            }
                            laborItemCount++;
                            break;
                            
                        case ENERGY:
                            if (item.getEnergyTariffId() != null) {
                                ext.vnua.veterinary_beapp.modules.pcost.model.EnergyTariff energyTariff = energyTariffMap.get(item.getEnergyTariffId());
                                if (energyTariff != null) {
                                    itemCode = energyTariff.getCode();
                                    itemName = energyTariff.getName();
                                    if (energyTariff.getUnitOfMeasure() != null) {
                                        unit = energyTariff.getUnitOfMeasure().getName();
                                    }
                                }
                            }
                            energyItemCount++;
                            break;
                    }
                    
                    materialItem.setItemCode(itemCode);
                    materialItem.setItemName(itemName);
                    materialItem.setUnit(unit != null ? unit : "");
                    
                    // Quantity calculation
                    BigDecimal baseQuantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                    BigDecimal scaledQuantity = baseQuantity.multiply(scaleFactor).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
                    
                    materialItem.setBaseQuantity(baseQuantity);
                    materialItem.setScaledQuantity(scaledQuantity);
                    materialItem.setScaleFactor(scaleFactor);
                    // materialItem.setNotes(item.getNotes()); // ProductionCostItem doesn't have notes field
                    
                    detail.getItems().add(materialItem);
                    totalItems++;
                }
            }
            
            result.getProductCosts().add(detail);
        }
        
        // Set summary
        ProductionCostMaterialDto.Summary summary = new ProductionCostMaterialDto.Summary();
        summary.setTotalProducts(result.getProductCosts().size());
        summary.setTotalItems(totalItems);
        summary.setMaterialItems(materialItemCount);
        summary.setLaborItems(laborItemCount);
        summary.setEnergyItems(energyItemCount);
        result.setSummary(summary);
        
        return result;
    }
    
    /**
     * Helper record to pair ProductionPlanProduct with its CostSheet
     */
    private record ProductWithCostSheet(ProductionPlanProduct planProduct, ProductionCostSheet costSheet) {}

    /**
     * Convert quantity from one unit to another
     * Supports: g, kg, mg, l, ml
     * 
     * @param value The value to convert
     * @param fromUnit Source unit
     * @param toUnit Target unit
     * @return Converted value
     */
    private BigDecimal convertToUnit(BigDecimal value, String fromUnit, String toUnit) {
        if (fromUnit == null || toUnit == null || fromUnit.equalsIgnoreCase(toUnit)) {
            return value;
        }
        
        // Normalize units to lowercase
        String from = fromUnit.toLowerCase().trim();
        String to = toUnit.toLowerCase().trim();
        
        if (from.equals(to)) {
            return value;
        }
        
        // Weight conversions (g as base unit)
        BigDecimal valueInGrams = switch (from) {
            case "kg" -> value.multiply(BigDecimal.valueOf(1000));
            case "g" -> value;
            case "mg" -> value.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            case "ton", "tấn" -> value.multiply(BigDecimal.valueOf(1_000_000));
            default -> value; // Unknown unit, return as-is
        };
        
        BigDecimal result = switch (to) {
            case "kg" -> valueInGrams.divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
            case "g" -> valueInGrams;
            case "mg" -> valueInGrams.multiply(BigDecimal.valueOf(1000));
            case "ton", "tấn" -> valueInGrams.divide(BigDecimal.valueOf(1_000_000), 6, RoundingMode.HALF_UP);
            default -> valueInGrams; // Unknown unit, return grams
        };
        
        return result;
    }

    private record PlanningContext(BigDecimal batchSize, LocalDate planDate) { }
}
