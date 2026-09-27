package com.example.controller;

import com.example.common.Result;
import com.example.entity.KnowledgeBase;
import com.example.service.KnowledgeBaseService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledgeBase")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody KnowledgeBase knowledgeBase) {
        knowledgeBaseService.add(knowledgeBase);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody KnowledgeBase knowledgeBase) {
        knowledgeBaseService.updateById(knowledgeBase);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        knowledgeBaseService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        knowledgeBaseService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(KnowledgeBase knowledgeBase) {
        List<KnowledgeBase> list = knowledgeBaseService.selectAll(knowledgeBase);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(KnowledgeBase knowledgeBase,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<KnowledgeBase> pageInfo = knowledgeBaseService.selectPage(knowledgeBase, pageNum, pageSize);
        return Result.success(pageInfo);
    }

}
