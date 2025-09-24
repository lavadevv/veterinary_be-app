package ext.vnua.veterinary_beapp.modules.production.dto;

import lombok.Data;

@Data
public class ProductionLineDto {
    private Long id;
    private String lineCode;
    private String name;
    private String description;
    private String status; // ACTIVE / INACTIVE
}