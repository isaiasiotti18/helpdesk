package com.helpdesk.backend.modules.category.application;

import com.helpdesk.backend.modules.category.application.dto.CategoryResponse;
import com.helpdesk.backend.modules.category.application.dto.CreateCategoryRequest;
import com.helpdesk.backend.modules.category.application.dto.UpdateCategoryRequest;
import com.helpdesk.backend.modules.category.domain.Category;
import com.helpdesk.backend.modules.category.domain.CategoryRepository;
import com.helpdesk.backend.modules.queue.domain.Queue;
import com.helpdesk.backend.modules.queue.domain.QueueRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final QueueRepository queueRepository;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException("Category name already exists", HttpStatus.CONFLICT);
        }

        Queue queue = null;
        if (request.queueId() != null) {
            queue = queueRepository.findById(request.queueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Queue", request.queueId()));
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .queue(queue)
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return CategoryResponse.from(category);
    }

    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (request.name() != null) {
            if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
                throw new BusinessException("Category name already exists", HttpStatus.CONFLICT);
            }
            category.setName(request.name());
        }

        if (request.description() != null) {
            category.setDescription(request.description());
        }

        if (request.queueId() != null) {
            Queue queue = queueRepository.findById(request.queueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Queue", request.queueId()));
            category.setQueue(queue);
        }

        if (request.isActive() != null) {
            if (request.isActive()) {
                category.activate();
            } else {
                category.deactivate();
            }
        }

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.deactivate();
        categoryRepository.save(category);
    }
}
