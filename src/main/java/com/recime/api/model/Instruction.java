package com.recime.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_instructions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "instruction_text", nullable = false, columnDefinition = "TEXT")
    private String instructionText;
}
