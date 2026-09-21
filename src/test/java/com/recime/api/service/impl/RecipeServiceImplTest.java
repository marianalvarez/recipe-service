package com.recime.api.service.impl;

import com.recime.api.dto.PageResponse;
import com.recime.api.dto.RecipeResponseDto;
import com.recime.api.dto.RecipeSearchCriteria;
import com.recime.api.exception.ResourceNotFoundException;
import com.recime.api.model.Recipe;
import com.recime.api.repository.RecipeRepository;
import com.recime.api.testdata.RecipeTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    @Test
    void createRecipePersistsMappedEntity() {
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe recipe = invocation.getArgument(0);
            recipe.setId(1L);
            return recipe;
        });

        RecipeResponseDto created = recipeService.createRecipe(RecipeTestData.pastaRequest());

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Vegetarian Pasta Primavera");
        assertThat(created.getVegetarian()).isTrue();
        assertThat(created.getIngredients()).hasSize(1);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void getRecipeByIdThrowsWhenMissing() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recipe not found with id: 99");
    }

    @Test
    void getAllRecipesExposesOneBasedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(recipeRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(RecipeTestData.pastaEntity(1L)), pageable, 1));

        PageResponse<RecipeResponseDto> response = recipeService.getAllRecipes(pageable);

        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getRecipes()).extracting(RecipeResponseDto::getName)
                .containsExactly("Vegetarian Pasta Primavera");
    }

    @Test
    void searchRecipesDelegatesToSpecificationQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        when(recipeRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Recipe>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(RecipeTestData.pastaEntity(1L)), pageable, 1));

        PageResponse<RecipeResponseDto> response = recipeService.searchRecipes(
                RecipeSearchCriteria.builder().vegetarian(true).build(), pageable);

        assertThat(response.getRecipes()).hasSize(1);
        verify(recipeRepository).findAll(org.mockito.ArgumentMatchers.<Specification<Recipe>>any(), any(Pageable.class));
    }

    @Test
    void updateRecipeLocksRowThenReplacesCollections() {
        Recipe existing = RecipeTestData.pastaEntity(1L);
        when(recipeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

        RecipeResponseDto updated = recipeService.updateRecipe(1L, RecipeTestData.adoboRequest());

        assertThat(updated.getName()).isEqualTo("Chicken Adobo");
        assertThat(updated.getVegetarian()).isFalse();
        assertThat(existing.getIngredients()).extracting("name").containsExactly("chicken");
        verify(recipeRepository).findByIdForUpdate(1L);
    }

    @Test
    void updateRecipeThrowsWhenMissing() {
        when(recipeRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.updateRecipe(1L, RecipeTestData.pastaRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRecipeLocksThenDeletes() {
        Recipe existing = RecipeTestData.pastaEntity(1L);
        when(recipeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(existing));

        recipeService.deleteRecipe(1L);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).findByIdForUpdate(1L);
        verify(recipeRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void deleteRecipeThrowsWhenMissing() {
        when(recipeRepository.findByIdForUpdate(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.deleteRecipe(8L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Recipe not found with id: 8");
    }
}
