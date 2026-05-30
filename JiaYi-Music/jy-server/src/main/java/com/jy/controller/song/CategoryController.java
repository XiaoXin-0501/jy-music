package com.jy.controller.song;

import com.jy.domain.ApiResult;
import com.jy.service.CategoryService;
import com.jy.vo.responseVo.CategoryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/song/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping()
    public ApiResult<List<CategoryInfo>> category() {
        List<CategoryInfo> categorylist = categoryService.getCategoryList();
        return ApiResult.success(categorylist);
    }
}
