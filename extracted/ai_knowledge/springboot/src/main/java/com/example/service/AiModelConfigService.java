package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.AiModelConfig;
import com.example.mapper.AiModelConfigMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiModelConfigService {

    @Resource
    private AiModelConfigMapper aiModelConfigMapper;

    public void add(AiModelConfig aiModelConfig) {
        if (StrUtil.isBlank(aiModelConfig.getEnabled())) {
            aiModelConfig.setEnabled("是");
        }
        if (StrUtil.isBlank(aiModelConfig.getModelType())) {
            aiModelConfig.setModelType("聊天模型");
        }
        aiModelConfig.setCreateTime(DateUtil.now());
        aiModelConfig.setUpdateTime(DateUtil.now());
        aiModelConfigMapper.insert(aiModelConfig);
    }

    public void updateById(AiModelConfig aiModelConfig) {
        aiModelConfig.setUpdateTime(DateUtil.now());
        aiModelConfigMapper.updateById(aiModelConfig);
    }

    public void deleteById(Integer id) {
        aiModelConfigMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            deleteById(id);
        }
    }

    public List<AiModelConfig> selectAll(AiModelConfig aiModelConfig) {
        return aiModelConfigMapper.selectAll(aiModelConfig);
    }

    public PageInfo<AiModelConfig> selectPage(AiModelConfig aiModelConfig, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AiModelConfig> list = aiModelConfigMapper.selectAll(aiModelConfig);
        return PageInfo.of(list);
    }
}
