package com.example.controller;

import com.example.common.Result;
import com.example.entity.KnowledgeSegment;
import com.example.entity.VectorizeRequest;
import com.example.service.KnowledgeSegmentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledgeSegment")
public class KnowledgeSegmentController {

    @Resource
    private KnowledgeSegmentService knowledgeSegmentService;

    /**
     * 生成知识片段
     */
    @PostMapping("/generate/{documentId}")
    public Result generate(@PathVariable Integer documentId) {
        List<KnowledgeSegment> list = knowledgeSegmentService.generateByDocumentId(documentId);
        return Result.success(list);
    }

    /**
     * 单个知识片段向量化
     */
    @PostMapping("/vectorize/{id}")
    public Result vectorize(@PathVariable Integer id, @RequestParam Integer modelConfigId) {
        knowledgeSegmentService.vectorizeById(id, modelConfigId);
        return Result.success();
    }

    /**
     * 批量知识片段向量化
     */
    @PostMapping("/vectorize/batch")
    public Result vectorizeBatch(@RequestBody VectorizeRequest request) {
        knowledgeSegmentService.vectorizeBatch(request.getIds(), request.getModelConfigId());
        return Result.success();
    }

    /**
     * 单个删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        knowledgeSegmentService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids) {
        knowledgeSegmentService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(KnowledgeSegment knowledgeSegment,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<KnowledgeSegment> pageInfo = knowledgeSegmentService.selectPage(knowledgeSegment, pageNum, pageSize);
        return Result.success(pageInfo);
    }
}
