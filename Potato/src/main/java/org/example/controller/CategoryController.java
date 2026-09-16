package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.result.Result;
import org.example.entity.Category;
import org.example.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口（公开）
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 启用中的分类列表 */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.ok(categoryService.listEnabled());
    }
}
