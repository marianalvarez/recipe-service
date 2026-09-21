package com.recime.api.service;

import com.recime.api.dto.PageResponse;
import com.recime.api.dto.RecipeRequestDto;
import com.recime.api.dto.RecipeResponseDto;
import com.recime.api.dto.RecipeSearchCriteria;
import org.springframework.data.domain.Pageable;

public interface RecipeService {
    RecipeResponseDto createRecipe(RecipeRequestDto requestDto);
    RecipeResponseDto getRecipeById(Long id);
    PageResponse<RecipeResponseDto> getAllRecipes(Pageable pageable);
    PageResponse<RecipeResponseDto> searchRecipes(RecipeSearchCriteria criteria, Pageable pageable);
    RecipeResponseDto updateRecipe(Long id, RecipeRequestDto requestDto);
    void deleteRecipe(Long id);
}
