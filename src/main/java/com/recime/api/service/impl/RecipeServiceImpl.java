package com.recime.api.service.impl;

import com.recime.api.dto.*;
import com.recime.api.exception.ResourceNotFoundException;
import com.recime.api.model.Ingredient;
import com.recime.api.model.Instruction;
import com.recime.api.model.Recipe;
import com.recime.api.repository.RecipeRepository;
import com.recime.api.repository.RecipeSpecification;
import com.recime.api.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;

    @Override
    @Transactional
    public RecipeResponseDto createRecipe(RecipeRequestDto requestDto) {
        Recipe recipe = mapToEntity(requestDto);
        Recipe saved = recipeRepository.save(recipe);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponseDto getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
        return mapToDto(recipe);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RecipeResponseDto> getAllRecipes(Pageable pageable) {
        return PageResponse.from(recipeRepository.findAll(pageable).map(this::mapToDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RecipeResponseDto> searchRecipes(RecipeSearchCriteria criteria, Pageable pageable) {
        Specification<Recipe> spec = RecipeSpecification.buildSpecification(criteria);
        return PageResponse.from(recipeRepository.findAll(spec, pageable).map(this::mapToDto));
    }

    @Override
    @Transactional
    public RecipeResponseDto updateRecipe(Long id, RecipeRequestDto requestDto) {
        Recipe recipe = recipeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        recipe.setName(requestDto.getName());
        recipe.setVegetarian(requestDto.getVegetarian());
        recipe.setServings(requestDto.getServings());
        recipe.replaceIngredients(toIngredients(requestDto.getIngredients()));
        recipe.replaceInstructions(toInstructions(requestDto.getInstructions()));

        return mapToDto(recipe);
    }

    @Override
    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
        recipeRepository.delete(recipe);
    }

    private Recipe mapToEntity(RecipeRequestDto dto) {
        Recipe recipe = Recipe.builder()
                .name(dto.getName())
                .vegetarian(dto.getVegetarian())
                .servings(dto.getServings())
                .build();

        recipe.replaceIngredients(toIngredients(dto.getIngredients()));
        recipe.replaceInstructions(toInstructions(dto.getInstructions()));

        return recipe;
    }

    private List<Ingredient> toIngredients(List<IngredientDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(i -> Ingredient.builder()
                        .name(i.getName())
                        .quantity(i.getQuantity())
                        .unit(i.getUnit())
                        .build())
                .toList();
    }

    private List<Instruction> toInstructions(List<InstructionDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(i -> Instruction.builder()
                        .stepNumber(i.getStepNumber())
                        .instructionText(i.getInstructionText())
                        .build())
                .toList();
    }

    private RecipeResponseDto mapToDto(Recipe entity) {
        List<IngredientDto> ingredientDtos = entity.getIngredients().stream()
                .map(i -> IngredientDto.builder().name(i.getName()).quantity(i.getQuantity()).unit(i.getUnit()).build())
                .toList();

        List<InstructionDto> instructionDtos = entity.getInstructions().stream()
                .map(i -> InstructionDto.builder().stepNumber(i.getStepNumber()).instructionText(i.getInstructionText()).build())
                .toList();

        return RecipeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .vegetarian(entity.getVegetarian())
                .servings(entity.getServings())
                .ingredients(ingredientDtos)
                .instructions(instructionDtos)
                .build();
    }
}
