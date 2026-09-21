package com.recime.api.repository;

import com.recime.api.dto.RecipeSearchCriteria;
import com.recime.api.model.Ingredient;
import com.recime.api.model.Instruction;
import com.recime.api.model.Recipe;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class RecipeSpecification {

    private RecipeSpecification() {
    }

    public static Specification<Recipe> buildSpecification(RecipeSearchCriteria criteria) {
        return Specification.allOf(
                isVegetarian(criteria.getVegetarian()),
                hasServings(criteria.getServings()),
                instructionContains(criteria.getInstructionSearch()),
                includesAllIngredients(criteria.getIncludeIngredients()),
                excludesIngredients(criteria.getExcludeIngredients())
        );
    }

    private static Specification<Recipe> isVegetarian(Boolean vegetarian) {
        if (vegetarian == null) {
            return null;
        }
        return (recipe, query, criteriaBuilder) ->
                criteriaBuilder.equal(recipe.get("vegetarian"), vegetarian);
    }

    private static Specification<Recipe> hasServings(Integer servings) {
        if (servings == null) {
            return null;
        }
        return (recipe, query, criteriaBuilder) ->
                criteriaBuilder.equal(recipe.get("servings"), servings);
    }

    private static Specification<Recipe> instructionContains(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return null;
        }
        return (recipe, query, criteriaBuilder) -> {
            distinctResults(query);
            Join<Recipe, Instruction> instruction = recipe.join("instructions", JoinType.INNER);
            return criteriaBuilder.like(
                    criteriaBuilder.lower(instruction.get("instructionText")),
                    "%" + searchText.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Recipe> includesAllIngredients(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return null;
        }
        return ingredientNames.stream()
                .map(RecipeSpecification::hasIngredient)
                .reduce(Specification::and)
                .orElse(null);
    }

    private static Specification<Recipe> hasIngredient(String ingredientName) {
        return (recipe, query, criteriaBuilder) -> {
            distinctResults(query);
            Join<Recipe, Ingredient> ingredient = recipe.join("ingredients", JoinType.INNER);
            return ingredientNameEquals(criteriaBuilder, ingredient, ingredientName);
        };
    }

    private static Specification<Recipe> excludesIngredients(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return null;
        }
        return ingredientNames.stream()
                .map(RecipeSpecification::doesNotHaveIngredient)
                .reduce(Specification::and)
                .orElse(null);
    }

    private static Specification<Recipe> doesNotHaveIngredient(String ingredientName) {
        return (recipe, query, criteriaBuilder) -> {
            if (query == null) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Long> recipesWithIngredient = query.subquery(Long.class);
            Root<Recipe> otherRecipe = recipesWithIngredient.from(Recipe.class);
            Join<Recipe, Ingredient> ingredient = otherRecipe.join("ingredients", JoinType.INNER);

            recipesWithIngredient.select(otherRecipe.get("id"))
                    .where(ingredientNameEquals(criteriaBuilder, ingredient, ingredientName));

            return criteriaBuilder.not(recipe.get("id").in(recipesWithIngredient));
        };
    }

    private static Predicate ingredientNameEquals(
            CriteriaBuilder criteriaBuilder,
            Join<Recipe, Ingredient> ingredient,
            String ingredientName) {
        return criteriaBuilder.equal(
                criteriaBuilder.lower(ingredient.get("name")),
                ingredientName.toLowerCase()
        );
    }

    private static void distinctResults(CriteriaQuery<?> query) {
        if (query != null) {
            query.distinct(true);
        }
    }
}
