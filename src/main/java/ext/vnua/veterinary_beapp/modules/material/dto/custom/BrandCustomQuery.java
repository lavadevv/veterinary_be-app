package ext.vnua.veterinary_beapp.modules.material.dto.custom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandCustomQuery {
    private Integer page = 0;
    private Integer size = 10;
    private String keywords;
    private String sortField = "name";
    private String sortType = "ASC";
}