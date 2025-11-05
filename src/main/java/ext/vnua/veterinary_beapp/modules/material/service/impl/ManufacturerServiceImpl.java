package ext.vnua.veterinary_beapp.modules.material.service.impl;

import ext.vnua.veterinary_beapp.exception.DataExistException;
import ext.vnua.veterinary_beapp.exception.MyCustomException;
import ext.vnua.veterinary_beapp.modules.audits.common.Auditable;
import ext.vnua.veterinary_beapp.modules.audits.enums.AuditAction;
import ext.vnua.veterinary_beapp.modules.material.dto.entity.ManufacturerDto;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.CreateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.dto.request.manufacturer.UpdateManufacturerRequest;
import ext.vnua.veterinary_beapp.modules.material.mapper.ManufacturerMapper;
import ext.vnua.veterinary_beapp.modules.material.model.Manufacturer;
import ext.vnua.veterinary_beapp.modules.material.repository.ManufacturerRepository;
import ext.vnua.veterinary_beapp.modules.material.repository.custom.CustomManufacturerQuery;
import ext.vnua.veterinary_beapp.modules.material.service.ManufacturerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final ManufacturerMapper manufacturerMapper;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Manufacturer> getAllManufacturer(CustomManufacturerQuery.ManuFilterParam param, PageRequest pageRequest) {
        Specification<Manufacturer> spec = CustomManufacturerQuery.getFilterManufacturer(param);
        return manufacturerRepository.findAll(spec, pageRequest);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ManufacturerDto selectById(Long id) {
        Manufacturer manu = manufacturerRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Nhà sản xuất không tồn tại"));
        return manufacturerMapper.toManufacturerDto(manu);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ManufacturerDto selectByCode(String manufacturerCode) {
        Manufacturer manu = manufacturerRepository.findByManufacturerCode(manufacturerCode)
                .orElseThrow(() -> new DataExistException("Mã nhà sản xuất không tồn tại"));
        return manufacturerMapper.toManufacturerDto(manu);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ManufacturerDto> selectActive() {
        return manufacturerRepository.findByIsActiveTrue()
                .stream().map(manufacturerMapper::toManufacturerDto).toList();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Manufacturer", description = "Tạo mới nhà sản xuất")
    public ManufacturerDto create(CreateManufacturerRequest req) {
        if (manufacturerRepository.findByManufacturerCode(req.getManufacturerCode()).isPresent()) {
            throw new DataExistException("Mã nhà sản xuất đã tồn tại");
        }
        try {
            Manufacturer entity = manufacturerMapper.toCreateEntity(req);
            entity.setIsActive(true);
            return manufacturerMapper.toManufacturerDto(manufacturerRepository.saveAndFlush(entity));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình thêm nhà sản xuất");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Manufacturer", description = "Cập nhật nhà sản xuất")
    public ManufacturerDto update(UpdateManufacturerRequest req) {
        Manufacturer current = manufacturerRepository.findById(req.getId())
                .orElseThrow(() -> new DataExistException("Nhà sản xuất không tồn tại"));

        // unique code
        Optional<Manufacturer> dup = manufacturerRepository.findByManufacturerCodeAndIdNot(req.getManufacturerCode(), req.getId());
        if (dup.isPresent()) {
            throw new DataExistException("Mã nhà sản xuất đã tồn tại");
        }

        try {
            manufacturerMapper.updateEntityFromRequest(req, current);
            return manufacturerMapper.toManufacturerDto(manufacturerRepository.saveAndFlush(current));
        } catch (Exception e) {
            throw new MyCustomException("Có lỗi xảy ra trong quá trình cập nhật nhà sản xuất");
        }
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Manufacturer manu = manufacturerRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Nhà sản xuất không tồn tại"));
        manu.setIsActive(!Boolean.TRUE.equals(manu.getIsActive()));
        manufacturerRepository.saveAndFlush(manu);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "Manufacturer", description = "Xoá nhà sản xuất")
    public void delete(Long id) {
        Manufacturer manu = manufacturerRepository.findById(id)
                .orElseThrow(() -> new DataExistException("Nhà sản xuất không tồn tại"));
        // Nếu đang được tham chiếu ở Supplier, DB sẽ chặn (FK). Có thể thêm check ở đây nếu muốn.
        try {
            manufacturerRepository.delete(manu);
        } catch (Exception e) {
            throw new MyCustomException("Không thể xoá nhà sản xuất (có thể đang được tham chiếu)");
        }
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "Manufacturer", description = "Xoá danh sách nhà sản xuất")
    public List<ManufacturerDto> deleteAllByIds(List<Long> ids) {
        List<ManufacturerDto> out = new ArrayList<>();
        for (Long id : ids) {
            Manufacturer manu = manufacturerRepository.findById(id)
                    .orElseThrow(() -> new MyCustomException("Không tìm thấy NSX id=" + id));
            out.add(manufacturerMapper.toManufacturerDto(manu));
            manufacturerRepository.delete(manu);
        }
        return out;
    }
}
