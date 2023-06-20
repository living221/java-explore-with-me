package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.CategoryMapper;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.category.CategoryMapper.toCategory;
import static ru.practicum.category.CategoryMapper.toCategoryDto;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = toCategory(newCategoryDto);

        return toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long catId) {
        categoryRepository.findById(catId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Category with id=%s was not found", catId));
        });

        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryById(Long catId, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Category with id=%s was not found", catId));
        });

        category.setName(newCategoryDto.getName());

        return toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {

        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Category with id=%s was not found", catId));
        });

        return toCategoryDto(category);
    }
}
