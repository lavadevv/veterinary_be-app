package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

@Data
public class ManufacturerDto {
    private Long id;
    private String manufacturerCode;
    private String manufacturerName;
    private String countryOfOrigin;
    private String officialDistributorName;
    private String officialDistributorPhone;
    private Boolean isActive;
    private String notes;
}
