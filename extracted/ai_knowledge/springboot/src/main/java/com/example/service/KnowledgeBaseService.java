package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.KnowledgeBase;
import com.example.exception.CustomException;
import com.example.mapper.KnowledgeBaseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP_SIZE = 80;

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    public void add(KnowledgeBase knowledgeBase) {
        KnowledgeBase dbKnowledgeBase = knowledgeBaseMapper.selectByCode(knowledgeBase.getCode());
        if (ObjectUtil.isNotNull(dbKnowledgeBase)) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "知识库编码已存在");
        }
        if (ObjectUtil.isEmpty(knowledgeBase.getStatus())) {
            knowledgeBase.setStatus("启用");
        }
        fillAndValidateChunkConfig(knowledgeBase);
        String now = DateUtil.now();
        knowledgeBase.setCreateTime(now);
        knowledgeBase.setUpdateTime(now);
        knowledgeBaseMapper.insert(knowledgeBase);
    }

    public void updateById(KnowledgeBase knowledgeBase) {
        KnowledgeBase dbKnowledgeBase = knowledgeBaseMapper.selectByCode(knowledgeBase.getCode());
        if (ObjectUtil.isNotNull(dbKnowledgeBase) && !dbKnowledgeBase.getId().equals(knowledgeBase.getId())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "知识库编码已存在");
        }
        fillAndValidateChunkConfig(knowledgeBase);
        knowledgeBase.setUpdateTime(DateUtil.now());
        knowledgeBaseMapper.updateById(knowledgeBase);
    }

    public void deleteById(Integer id) {
        knowledgeBaseMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            knowledgeBaseMapper.deleteById(id);
        }
    }

    public List<KnowledgeBase> selectAll(KnowledgeBase knowledgeBase) {
        return knowledgeBaseMapper.selectAll(knowledgeBase);
    }

    public PageInfo<KnowledgeBase> selectPage(KnowledgeBase knowledgeBase, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<KnowledgeBase> list = knowledgeBaseMapper.selectAll(knowledgeBase);
        return PageInfo.of(list);
    }

    private void fillAndValidateChunkConfig(KnowledgeBase knowledgeBase) {
        if (ObjectUtil.isEmpty(knowledgeBase.getChunkSize())) {
            knowledgeBase.setChunkSize(DEFAULT_CHUNK_SIZE);
        }
        if (ObjectUtil.isEmpty(knowledgeBase.getOverlapSize())) {
            knowledgeBase.setOverlapSize(DEFAULT_OVERLAP_SIZE);
        }
        if (knowledgeBase.getChunkSize() <= 0) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "切片长度必须大于0");
        }
        if (knowledgeBase.getOverlapSize() < 0) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "重叠长度不能小于0");
        }
        if (knowledgeBase.getOverlapSize() >= knowledgeBase.getChunkSize()) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR.code, "重叠长度必须小于切片长度");
        }
    }
}
