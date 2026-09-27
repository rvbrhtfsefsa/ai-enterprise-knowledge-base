package com.example.controller;

import com.example.common.Result;
import com.example.entity.KnowledgeDocument;
import com.example.service.KnowledgeDocumentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledgeDocument")
public class KnowledgeDocumentController {

    @Resource
    private KnowledgeDocumentService knowledgeDocumentService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody KnowledgeDocument knowledgeDocument) {
        knowledgeDocumentService.add(knowledgeDocument);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody KnowledgeDocument knowledgeDocument) {
        knowledgeDocumentService.updateById(knowledgeDocument);
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        knowledgeDocumentService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        knowledgeDocumentService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(KnowledgeDocument knowledgeDocument) {
        List<KnowledgeDocument> list = knowledgeDocumentService.selectAll(knowledgeDocument);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(KnowledgeDocument knowledgeDocument,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<KnowledgeDocument> pageInfo = knowledgeDocumentService.selectPage(knowledgeDocument, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 解析文档
     */
    @PostMapping("/parse/{id}")
    public Result parse(@PathVariable Integer id) {
        KnowledgeDocument knowledgeDocument = knowledgeDocumentService.parseById(id);
        return Result.success(knowledgeDocument);
    }

}
