package com.recime.api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDto {
    private Long id;
    private String name;
    private Boolean vegetarian;
    private Integer servings;
    private List<IngredientDto> ingredients;
    private List<InstructionDto> instructions;
}
