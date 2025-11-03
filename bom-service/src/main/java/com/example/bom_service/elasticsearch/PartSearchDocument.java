package com.example.bom_service.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Elasticsearch document model for searching Parts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "parts")
public class PartSearchDocument {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String level;
    
    @Field(type = FieldType.Keyword)
    private String creator;
    
    @Field(type = FieldType.Keyword)
    private String stage;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;
    
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;
    
    @Field(type = FieldType.Boolean)
    private Boolean deleted;
    
    /**
     * Utility method to map from Part entity to search document
     */
    public static PartSearchDocument fromPart(com.example.bom_service.model.Part part) {
        PartSearchDocument searchDoc = new PartSearchDocument();
        searchDoc.setId(part.getId());
        searchDoc.setTitle(part.getTitle());
        searchDoc.setDescription(part.getDescription());
        searchDoc.setLevel(part.getLevel());
        searchDoc.setCreator(part.getCreator());
        searchDoc.setStage(part.getStage() != null ? part.getStage().name() : null);
        searchDoc.setStatus(part.getStatus() != null ? part.getStatus().name() : null);
        searchDoc.setCreateTime(part.getCreateTime());
        searchDoc.setUpdateTime(part.getUpdateTime());
        searchDoc.setDeleted(part.isDeleted());
        return searchDoc;
    }
}



