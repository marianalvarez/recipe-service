package com.recime.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean vegetarian;

    @Column(nullable = false)
    private Integer servings;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    @Builder.Default
    private List<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    @BatchSize(size = 25)
    @Builder.Default
    private List<Instruction> instructions = new ArrayList<>();

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
        instruction.setRecipe(this);
    }

    public void replaceIngredients(List<Ingredient> newIngredients) {
        ingredients.clear();
        if (newIngredients != null) {
            newIngredients.forEach(this::addIngredient);
        }
    }

    public void replaceInstructions(List<Instruction> newInstructions) {
        instructions.clear();
        if (newInstructions != null) {
            newInstructions.forEach(this::addInstruction);
        }
    }
}
