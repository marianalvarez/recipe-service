package com.recime.api.testdata;

import com.recime.api.dto.IngredientDto;
import com.recime.api.dto.InstructionDto;
import com.recime.api.dto.RecipeRequestDto;
import com.recime.api.dto.RecipeResponseDto;
import com.recime.api.model.Ingredient;
import com.recime.api.model.Instruction;
import com.recime.api.model.Recipe;

import java.math.BigDecimal;
import java.util.List;

public final class RecipeTestData {

    private RecipeTestData() {
    }

    public static RecipeRequestDto pastaRequest() {
        return RecipeRequestDto.builder()
                .name("Vegetarian Pasta Primavera")
                .vegetarian(true)
                .servings(4)
                .ingredients(List.of(ingredient("pasta", "250", "grams")))
                .instructions(List.of(instruction(1, "Boil pasta in salted water for 10 minutes.")))
                .build();
    }

    public static RecipeRequestDto adoboRequest() {
        return RecipeRequestDto.builder()
                .name("Chicken Adobo")
                .vegetarian(false)
                .servings(4)
                .ingredients(List.of(ingredient("chicken", "1", "kilogram")))
                .instructions(List.of(instruction(1, "Simmer chicken in soy sauce and vinegar.")))
                .build();
    }

    public static Recipe pastaEntity(Long id) {
        Recipe recipe = Recipe.builder()
                .id(id)
                .name("Vegetarian Pasta Primavera")
                .vegetarian(true)
                .servings(4)
                .build();
        recipe.addIngredient(Ingredient.builder()
                .name("pasta")
                .quantity(new BigDecimal("250"))
                .unit("grams")
                .build());
        recipe.addInstruction(Instruction.builder()
                .stepNumber(1)
                .instructionText("Boil pasta in salted water for 10 minutes.")
                .build());
        return recipe;
    }

    public static RecipeResponseDto pastaResponse(Long id) {
        return RecipeResponseDto.builder()
                .id(id)
                .name("Vegetarian Pasta Primavera")
                .vegetarian(true)
                .servings(4)
                .ingredients(List.of(ingredient("pasta", "250", "grams")))
                .instructions(List.of(instruction(1, "Boil pasta in salted water for 10 minutes.")))
                .build();
    }

    public static IngredientDto ingredient(String name, String quantity, String unit) {
        return IngredientDto.builder()
                .name(name)
                .quantity(new BigDecimal(quantity))
                .unit(unit)
                .build();
    }

    public static InstructionDto instruction(int stepNumber, String text) {
        return InstructionDto.builder()
                .stepNumber(stepNumber)
                .instructionText(text)
                .build();
    }
}
