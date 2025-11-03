package com.example.document_service.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Elasticsearch document model for searching documents
 * This is indexed to Elasticsearch for fast search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents")
public class DocumentSearchDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String documentNumber;
    
    @Field(type = FieldType.Keyword)
    private String masterId;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Keyword)
    private String stage;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private String contentType;
    
    @Field(type = FieldType.Keyword)
    private String creator;
    
    @Field(type = FieldType.Long)
    private Long fileSize;
    
    @Field(type = FieldType.Keyword)
    private String version;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
    
    @Field(type = FieldType.Boolean)
    private Boolean isActive;
    
    /**
     * Utility method to map from Document entity to search document
     */
    public static DocumentSearchDocument fromDocument(com.example.document_service.model.Document doc) {
        DocumentSearchDocument searchDoc = new DocumentSearchDocument();
        searchDoc.setId(doc.getId());
        searchDoc.setTitle(doc.getTitle());
        searchDoc.setDescription(doc.getDescription());
        // Use master ID as document number (or could use title)
        searchDoc.setDocumentNumber(doc.getMaster() != null ? doc.getMaster().getId() : null);
        searchDoc.setMasterId(doc.getMaster() != null ? doc.getMaster().getId() : null);
        searchDoc.setStatus(doc.getStatus() != null ? doc.getStatus().name() : null);
        searchDoc.setStage(doc.getStage() != null ? doc.getStage().name() : null);
        searchDoc.setCategory(doc.getMaster() != null ? doc.getMaster().getCategory() : null);
        searchDoc.setContentType(doc.getContentType());
        searchDoc.setCreator(doc.getCreator());
        searchDoc.setFileSize(doc.getFileSize());
        searchDoc.setVersion(doc.getVersion() != 0 ? String.valueOf(doc.getVersion()) : null);
        searchDoc.setCreateTime(doc.getCreateTime());
        // Use fileUploadedAt as updateTime (or could use createTime)
        searchDoc.setUpdateTime(doc.getFileUploadedAt() != null ? doc.getFileUploadedAt() : doc.getCreateTime());
        searchDoc.setIsActive(doc.isActive());
        return searchDoc;
    }
}

