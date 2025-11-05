package ext.vnua.veterinary_beapp.modules.material.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;
}