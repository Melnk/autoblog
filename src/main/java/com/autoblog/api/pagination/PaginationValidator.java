package com.autoblog.api.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PaginationValidator {

    static final int MAX_PAGE_SIZE = 100;

    public Pageable validateAndCreate(int page, int size, Sort sort) {
        if (page < 0) {
            throw new InvalidPaginationException("page", "Page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("size", "Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page, size, sort);
    }
}
