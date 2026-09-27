package com.example.mapper;

import com.example.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface KnowledgeDocumentMapper {

    int insert(KnowledgeDocument knowledgeDocument);

    void updateById(KnowledgeDocument knowledgeDocument);

    void deleteById(Integer id);

    @Select("select * from `knowledge_document` where id = #{id}")
    KnowledgeDocument selectById(Integer id);

    @Update("update `knowledge_document` set parsed_text = null, parse_time = null, parse_status = '待解析', vector_status = '未向量化', update_time = #{updateTime} where id = #{id}")
    void resetProcessById(@Param("id") Integer id, @Param("updateTime") String updateTime);

    @Update("update `knowledge_document` set vector_status = #{vectorStatus}, update_time = #{updateTime} where id = #{id}")
    void updateVectorStatus(@Param("id") Integer id, @Param("vectorStatus") String vectorStatus, @Param("updateTime") String updateTime);

    List<KnowledgeDocument> selectAll(KnowledgeDocument knowledgeDocument);

}
