package com.recime.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeRequestDto {

    @NotBlank(message = "Recipe name is required")
    private String name;

    @NotNull(message = "Vegetarian indicator is required")
    private Boolean vegetarian;

    @NotNull(message = "Servings is required")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;

    @NotEmpty(message = "Ingredients list cannot be empty")
    @Valid
    private List<IngredientDto> ingredients;

    @NotEmpty(message = "Instructions list cannot be empty")
    @Valid
    private List<InstructionDto> instructions;
}
