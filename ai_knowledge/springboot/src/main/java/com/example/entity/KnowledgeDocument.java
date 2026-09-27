package com.example.entity;

public class KnowledgeDocument {

    /** 主键ID */
    private Integer id;
    /** 知识库ID */
    private Integer knowledgeBaseId;
    /** 知识库名称 */
    private String knowledgeBaseName;
    /** 文档标题 */
    private String title;
    /** 文件名称 */
    private String fileName;
    /** 文件地址 */
    private String fileUrl;
    /** 文件类型 */
    private String fileType;
    /** 文件大小 */
    private String fileSize;
    /** 解析文本 */
    private String parsedText;
    /** 解析状态 */
    private String parseStatus;
    /** 解析时间 */
    private String parseTime;
    /** 向量化状态 */
    private String vectorStatus;
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

    public Integer getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Integer knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public String getKnowledgeBaseName() {
        return knowledgeBaseName;
    }

    public void setKnowledgeBaseName(String knowledgeBaseName) {
        this.knowledgeBaseName = knowledgeBaseName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getParsedText() {
        return parsedText;
    }

    public void setParsedText(String parsedText) {
        this.parsedText = parsedText;
    }

    public String getParseStatus() {
        return parseStatus;
    }

    public void setParseStatus(String parseStatus) {
        this.parseStatus = parseStatus;
    }

    public String getParseTime() {
        return parseTime;
    }

    public void setParseTime(String parseTime) {
        this.parseTime = parseTime;
    }

    public String getVectorStatus() {
        return vectorStatus;
    }

    public void setVectorStatus(String vectorStatus) {
        this.vectorStatus = vectorStatus;
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
