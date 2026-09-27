package com.example.entity;

public class KnowledgeBase {

    /** 主键ID */
    private Integer id;
    /** 知识库名称 */
    private String name;
    /** 知识库编码 */
    private String code;
    /** 知识库描述 */
    private String description;
    /** 切片长度 */
    private Integer chunkSize;
    /** 重叠长度 */
    private Integer overlapSize;
    /** 启用状态 */
    private String status;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Integer getOverlapSize() {
        return overlapSize;
    }

    public void setOverlapSize(Integer overlapSize) {
        this.overlapSize = overlapSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
