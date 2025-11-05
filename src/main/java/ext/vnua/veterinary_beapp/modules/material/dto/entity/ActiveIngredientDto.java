package ext.vnua.veterinary_beapp.modules.material.dto.entity;

import lombok.Data;

@Data
public class ActiveIngredientDto {
    private Long id;
    private String ingredientCode;
    private String ingredientName;
    private String casNumber;
    private String description;
    private Boolean isActive;
}