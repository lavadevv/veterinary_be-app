package ext.vnua.veterinary_beapp.modules.material.dto.request.activeIngredient;

import lombok.Data;

@Data
public class GetActiveIngredientRequest {
    private int start = 0;
    private int limit = 10;
    private String search;
    private String sortField = "ingredientName";
    private String sortDirection = "ASC";
}