package com.example.mapper;

import com.example.entity.KnowledgeSegment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface KnowledgeSegmentMapper {

    int insert(KnowledgeSegment knowledgeSegment);

    void deleteById(Integer id);

    void deleteByDocumentId(Integer documentId);

    @Select("select * from `knowledge_segment` where id = #{id}")
    KnowledgeSegment selectById(Integer id);

    @Update("update `knowledge_segment` set vector_status = #{vectorStatus}, update_time = #{updateTime} where id = #{id}")
    void updateVectorStatus(@Param("id") Integer id, @Param("vectorStatus") String vectorStatus, @Param("updateTime") String updateTime);

    List<KnowledgeSegment> selectAll(KnowledgeSegment knowledgeSegment);

}
