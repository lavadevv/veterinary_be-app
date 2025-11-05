package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrandDto {
    private Long id;
    private String name;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
