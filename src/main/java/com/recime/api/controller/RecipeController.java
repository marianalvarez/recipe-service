package com.recime.api.controller;

import com.recime.api.dto.PageResponse;
import com.recime.api.dto.RecipeRequestDto;
import com.recime.api.dto.RecipeResponseDto;
import com.recime.api.dto.RecipeSearchCriteria;
import com.recime.api.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponseDto> createRecipe(@Valid @RequestBody RecipeRequestDto requestDto) {
        return new ResponseEntity<>(recipeService.createRecipe(requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<RecipeResponseDto>> getAllRecipes(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(recipeService.getAllRecipes(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<RecipeResponseDto>> searchRecipes(
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) Integer servings,
            @RequestParam(required = false) List<String> includeIngredients,
            @RequestParam(required = false) List<String> excludeIngredients,
            @RequestParam(required = false) String instructionSearch,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        RecipeSearchCriteria criteria = RecipeSearchCriteria.builder()
                .vegetarian(vegetarian)
                .servings(servings)
                .includeIngredients(includeIngredients)
                .excludeIngredients(excludeIngredients)
                .instructionSearch(instructionSearch)
                .build();

        return ResponseEntity.ok(recipeService.searchRecipes(criteria, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequestDto requestDto) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
