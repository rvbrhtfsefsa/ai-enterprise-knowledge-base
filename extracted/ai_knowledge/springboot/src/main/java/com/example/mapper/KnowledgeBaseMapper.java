package com.example.mapper;

import com.example.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeBaseMapper {

    int insert(KnowledgeBase knowledgeBase);

    void updateById(KnowledgeBase knowledgeBase);

    void deleteById(Integer id);

    @Select("select * from `knowledge_base` where id = #{id}")
    KnowledgeBase selectById(Integer id);

    @Select("select * from `knowledge_base` where code = #{code}")
    KnowledgeBase selectByCode(String code);

    List<KnowledgeBase> selectAll(KnowledgeBase knowledgeBase);

}
