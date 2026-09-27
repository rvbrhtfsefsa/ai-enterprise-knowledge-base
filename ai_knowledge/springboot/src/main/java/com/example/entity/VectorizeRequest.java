package com.example.entity;

import java.util.List;

public class VectorizeRequest {

    /** 向量模型配置ID */
    private Integer modelConfigId;
    /** 知识片段ID集合 */
    private List<Integer> ids;

    public Integer getModelConfigId() {
        return modelConfigId;
    }

    public void setModelConfigId(Integer modelConfigId) {
        this.modelConfigId = modelConfigId;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
