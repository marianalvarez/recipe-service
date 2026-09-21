package com.recime.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> recipes;
    private long totalCount;
    private int page;

    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(source.getContent(), source.getTotalElements(), source.getNumber() + 1);
    }
}
