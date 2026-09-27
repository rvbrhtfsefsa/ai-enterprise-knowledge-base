package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.config.PgVectorConfig;
import com.example.entity.KnowledgeSegment;
import com.example.entity.KnowledgeSource;
import com.example.exception.CustomException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PgVectorService {

    @Resource
    private PgVectorConfig pgVectorConfig;

    public void saveSegmentVector(KnowledgeSegment segment, List<Double> embedding) {
        initTable();
        String sql = "insert into " + pgVectorConfig.getTableName() + " " +
                "(segment_id, knowledge_base_id, document_id, content, embedding, create_time, update_time) " +
                "values (?, ?, ?, ?, ?::vector, ?, ?) " +
                "on conflict (segment_id) do update set " +
                "knowledge_base_id = excluded.knowledge_base_id, " +
                "document_id = excluded.document_id, " +
                "content = excluded.content, " +
                "embedding = excluded.embedding, " +
                "update_time = excluded.update_time";
        String now = DateUtil.now();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, segment.getId());
            statement.setInt(2, segment.getKnowledgeBaseId());
            statement.setInt(3, segment.getDocumentId());
            statement.setString(4, segment.getContent());
            statement.setString(5, toVectorLiteral(embedding));
            statement.setString(6, now);
            statement.setString(7, now);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "写入 PGVector 失败：" + e.getMessage());
        }
    }

    public void deleteBySegmentId(Integer segmentId) {
        String sql = "delete from " + pgVectorConfig.getTableName() + " where segment_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, segmentId);
            statement.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void deleteByDocumentId(Integer documentId) {
        String sql = "delete from " + pgVectorConfig.getTableName() + " where document_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentId);
            statement.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public List<KnowledgeSource> searchSimilar(List<Double> embedding, Integer topK, Double similarityThreshold) {
        initTable();
        String sql = "select segment_id, knowledge_base_id, document_id, content, " +
                "(1 - (embedding <=> ?::vector)) as similarity " +
                "from " + pgVectorConfig.getTableName() + " " +
                "where (1 - (embedding <=> ?::vector)) >= ? " +
                "order by embedding <=> ?::vector " +
                "limit ?";
        List<KnowledgeSource> sourceList = new ArrayList<>();
        String vector = toVectorLiteral(embedding);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, vector);
            statement.setString(2, vector);
            statement.setDouble(3, similarityThreshold);
            statement.setString(4, vector);
            statement.setInt(5, topK);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                KnowledgeSource source = new KnowledgeSource();
                source.setSegmentId(resultSet.getInt("segment_id"));
                source.setKnowledgeBaseId(resultSet.getInt("knowledge_base_id"));
                source.setDocumentId(resultSet.getInt("document_id"));
                source.setContent(resultSet.getString("content"));
                source.setSimilarity(resultSet.getDouble("similarity"));
                sourceList.add(source);
            }
            return sourceList;
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "PGVector 相似度检索失败：" + e.getMessage());
        }
    }

    public void initTable() {
        String sql = "create table if not exists " + pgVectorConfig.getTableName() + " (" +
                "id bigserial primary key, " +
                "segment_id int unique not null, " +
                "knowledge_base_id int, " +
                "document_id int, " +
                "content text, " +
                "embedding vector(" + pgVectorConfig.getDimension() + "), " +
                "create_time varchar(255), " +
                "update_time varchar(255)" +
                ")";
        String indexSql = "create index if not exists idx_" + pgVectorConfig.getTableName() + "_embedding " +
                "on " + pgVectorConfig.getTableName() + " using ivfflat (embedding vector_cosine_ops) with (lists = 100)";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("create extension if not exists vector");
            statement.execute(sql);
            statement.execute(indexSql);
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.SYSTEM_ERROR.code, "初始化 PGVector 表失败：" + e.getMessage());
        }
    }

    private Connection getConnection() throws Exception {
        return DriverManager.getConnection(pgVectorConfig.getUrl(), pgVectorConfig.getUsername(), pgVectorConfig.getPassword());
    }

    private String toVectorLiteral(List<Double> embedding) {
        return "[" + embedding.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }
}
