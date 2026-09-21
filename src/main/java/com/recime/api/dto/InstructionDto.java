package com.recime.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructionDto {

    @NotNull(message = "Step number is required")
    @Min(value = 1, message = "Step number must be at least 1")
    private Integer stepNumber;

    @NotBlank(message = "Instruction text is required")
    private String instructionText;
}
