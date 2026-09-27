package com.example.mapper;

import com.example.entity.AiModelConfig;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AiModelConfigMapper {

    int insert(AiModelConfig aiModelConfig);

    void updateById(AiModelConfig aiModelConfig);

    void deleteById(Integer id);

    @Select("select * from `ai_model_config` where id = #{id}")
    AiModelConfig selectById(Integer id);

    List<AiModelConfig> selectAll(AiModelConfig aiModelConfig);

}
