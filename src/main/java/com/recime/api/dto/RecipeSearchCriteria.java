package com.recime.api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeSearchCriteria {
    private Boolean vegetarian;
    private Integer servings;
    private List<String> includeIngredients;
    private List<String> excludeIngredients;
    private String instructionSearch;
}
