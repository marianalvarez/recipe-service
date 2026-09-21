package com.recime.api.controller;

import com.recime.api.config.TraceIdFilter;
import com.recime.api.dto.PageResponse;
import com.recime.api.dto.RecipeRequestDto;
import com.recime.api.exception.GlobalExceptionHandler;
import com.recime.api.exception.ResourceNotFoundException;
import com.recime.api.service.RecipeService;
import com.recime.api.testdata.RecipeTestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecipeController.class)
@Import({GlobalExceptionHandler.class, TraceIdFilter.class})
@TestPropertySource(properties = "spring.data.web.pageable.one-indexed-parameters=true")
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void createRecipeReturns201() throws Exception {
        when(recipeService.createRecipe(any(RecipeRequestDto.class)))
                .thenReturn(RecipeTestData.pastaResponse(1L));

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Trace-Id", "trace-create")
                        .content(objectMapper.writeValueAsString(RecipeTestData.pastaRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Trace-Id", "trace-create"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Vegetarian Pasta Primavera"));
    }

    @Test
    void createRecipeReturns400WhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","vegetarian":true,"servings":4,"ingredients":[],"instructions":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.details.name").value("Recipe name is required"));
    }

    @Test
    void getRecipeByIdReturns404WhenMissing() throws Exception {
        when(recipeService.getRecipeById(99L))
                .thenThrow(new ResourceNotFoundException("Recipe not found with id: 99"));

        mockMvc.perform(get("/api/v1/recipes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recipe not found with id: 99"));
    }

    @Test
    void listRecipesUsesOneBasedPageQuery() throws Exception {
        when(recipeService.getAllRecipes(any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(RecipeTestData.pastaResponse(1L)), 1, 1));

        mockMvc.perform(get("/api/v1/recipes").param("page", "1").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.recipes[0].name").value("Vegetarian Pasta Primavera"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(recipeService).getAllRecipes(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void searchRecipesPassesVegetarianFilter() throws Exception {
        when(recipeService.searchRecipes(any(), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 1));

        mockMvc.perform(get("/api/v1/recipes/search").param("vegetarian", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0));

        verify(recipeService).searchRecipes(ArgumentMatchers.argThat(criteria ->
                Boolean.TRUE.equals(criteria.getVegetarian())), any(Pageable.class));
    }

    @Test
    void updateRecipeReturns409OnLockTimeout() throws Exception {
        when(recipeService.updateRecipe(eq(1L), any(RecipeRequestDto.class)))
                .thenThrow(new CannotAcquireLockException("could not obtain lock"));

        mockMvc.perform(put("/api/v1/recipes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RecipeTestData.adoboRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Recipe was updated concurrently. Retry the request."));
    }

    @Test
    void deleteRecipeReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/recipes/1"))
                .andExpect(status().isNoContent());

        verify(recipeService).deleteRecipe(1L);
    }
}
