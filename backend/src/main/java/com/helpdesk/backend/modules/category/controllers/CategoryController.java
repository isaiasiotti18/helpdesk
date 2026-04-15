package com.helpdesk.backend.modules.category.controllers;

import com.helpdesk.backend.modules.category.application.CategoryService;
import com.helpdesk.backend.modules.category.application.dto.CategoryResponse;
import com.helpdesk.backend.modules.category.application.dto.CreateCategoryRequest;
import com.helpdesk.backend.modules.category.application.dto.UpdateCategoryRequest;
import com.helpdesk.backend.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(categoryService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listActive()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
