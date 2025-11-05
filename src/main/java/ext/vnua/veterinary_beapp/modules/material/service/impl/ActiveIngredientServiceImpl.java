package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.ActiveIngredientDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.CreateActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.GetActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient.UpdateActiveIngredientRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.ActiveIngredientMapper;
import ext.vnua.veterinary_beapp.modules.material.model.ActiveIngredient;
import ext.vnua.veterinary_beapp.modules.material.repository.ActiveIngredientRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.MaterialActiveIngredientRepository;
import ext.vnua.veterinary_beapp.modules.material.service.ActiveIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActiveIngredientServiceImpl implements ActiveIngredientService {

    private final ActiveIngredientRepository activeIngredientRepository;
    private final MaterialActiveIngredientRepository materialActiveIngredientRepository;
    private final ActiveIngredientMapper activeIngredientMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ActiveIngredient> search(GetActiveIngredientRequest request, Pageable pageable) {
        Specification<ActiveIngredient> spec = Specification.where(null);
        
        if (StringUtils.hasText(request.getSearch())) {
            String keyword = request.getSearch().trim().toLowerCase();
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("ingredientName")), "%" + keyword + "%"),
                    cb.like(cb.lower(root.get("ingredientCode")), "%" + keyword + "%"),
                    cb.like(cb.lower(root.get("casNumber")), "%" + keyword + "%")
                )
            );
        }
        
        return activeIngredientRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveIngredientDto> getActiveIngredients() {
        return activeIngredientRepository.findByIsActiveTrueOrderByIngredientNameAsc()
                .stream()
                .map(activeIngredientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActiveIngredientDto getActiveIngredientById(Long id) {
        ActiveIngredient activeIngredient = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Hoạt chất không tồn tại"));
        return activeIngredientMapper.toDto(activeIngredient);
    }

    @Override
    @Transactional(readOnly = true)
    public ActiveIngredientDto getActiveIngredientByCode(String code) {
        ActiveIngredient activeIngredient = activeIngredientRepository.findByIngredientCode(code)
                .orElseThrow(() -> new DataExistException("Mã hoạt chất không tồn tại"));
        return activeIngredientMapper.toDto(activeIngredient);
    }

    @Override
    public ActiveIngredientDto createActiveIngredient(CreateActiveIngredientRequest request) {
        // Validate unique constraints
        if (activeIngredientRepository.existsByIngredientCode(request.getIngredientCode())) {
            throw new IllegalArgumentException("Mã hoạt chất đã tồn tại");
        }
        
        if (activeIngredientRepository.existsByIngredientName(request.getIngredientName())) {
            throw new IllegalArgumentException("Tên hoạt chất đã tồn tại");
        }
        
        if (StringUtils.hasText(request.getCasNumber()) && 
            activeIngredientRepository.existsByCasNumber(request.getCasNumber())) {
            throw new IllegalArgumentException("Số CAS đã tồn tại");
        }

        ActiveIngredient activeIngredient = new ActiveIngredient();
        activeIngredient.setIngredientCode(request.getIngredientCode());
        activeIngredient.setIngredientName(request.getIngredientName());
        activeIngredient.setCasNumber(request.getCasNumber());
        activeIngredient.setDescription(request.getDescription());
        activeIngredient.setIsActive(request.getIsActive());

        ActiveIngredient saved = activeIngredientRepository.save(activeIngredient);
        return activeIngredientMapper.toDto(saved);
    }

    @Override
    public ActiveIngredientDto updateActiveIngredient(UpdateActiveIngredientRequest request) {
        ActiveIngredient activeIngredient = activeIngredientRepository.findById(request.getId())
                .orElseThrow(() -> new DataExistException("Hoạt chất không tồn tại"));

        // Validate unique constraints if values are being changed
        if (StringUtils.hasText(request.getIngredientCode()) && 
            !request.getIngredientCode().equals(activeIngredient.getIngredientCode()) &&
            activeIngredientRepository.existsByIngredientCodeAndIdNot(request.getIngredientCode(), request.getId())) {
            throw new IllegalArgumentException("Mã hoạt chất đã tồn tại");
        }
        
        if (StringUtils.hasText(request.getIngredientName()) && 
            !request.getIngredientName().equals(activeIngredient.getIngredientName()) &&
            activeIngredientRepository.existsByIngredientNameAndIdNot(request.getIngredientName(), request.getId())) {
            throw new IllegalArgumentException("Tên hoạt chất đã tồn tại");
        }
        
        if (StringUtils.hasText(request.getCasNumber()) && 
            !request.getCasNumber().equals(activeIngredient.getCasNumber()) &&
            activeIngredientRepository.existsByCasNumberAndIdNot(request.getCasNumber(), request.getId())) {
            throw new IllegalArgumentException("Số CAS đã tồn tại");
        }

        // Update fields if provided
        if (StringUtils.hasText(request.getIngredientCode())) {
            activeIngredient.setIngredientCode(request.getIngredientCode());
        }
        if (StringUtils.hasText(request.getIngredientName())) {
            activeIngredient.setIngredientName(request.getIngredientName());
        }
        if (request.getCasNumber() != null) {
            activeIngredient.setCasNumber(request.getCasNumber());
        }
        if (request.getDescription() != null) {
            activeIngredient.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            activeIngredient.setIsActive(request.getIsActive());
        }

        ActiveIngredient saved = activeIngredientRepository.save(activeIngredient);
        return activeIngredientMapper.toDto(saved);
    }

    @Override
    public void deleteActiveIngredient(Long id) {
        ActiveIngredient activeIngredient = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Hoạt chất không tồn tại"));
        
        // Check if it's being used in any materials
        long usageCount = materialActiveIngredientRepository.countMaterialsUsingActiveIngredient(id);
        if (usageCount > 0) {
            throw new DataIntegrityViolationException("Không thể xóa hoạt chất đang được sử dụng trong " + usageCount + " vật liệu");
        }
        
        activeIngredientRepository.delete(activeIngredient);
    }

    @Override
    public void toggleActiveStatus(Long id) {
        ActiveIngredient activeIngredient = activeIngredientRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Hoạt chất không tồn tại"));
        
        activeIngredient.setIsActive(!activeIngredient.getIsActive());
        activeIngredientRepository.save(activeIngredient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActiveIngredientDto> searchByKeyword(String keyword) {
        return activeIngredientRepository.searchByKeyword(keyword)
                .stream()
                .map(activeIngredientMapper::toDto)
                .collect(Collectors.toList());
    }
}