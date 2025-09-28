package com.example.plm.change.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Node("Change")
public class ChangeNode {

    @Id
    private String id;

    private String title;
    private String stage;
    private String changeClass;
    private String product;
    private String status;
    private String creator;
    private LocalDateTime createTime;
    private String changeReason;

    @Relationship(type = "AFFECTS_DOCUMENT", direction = Relationship.Direction.OUTGOING)
    private Set<DocumentNode> affectedDocuments = new HashSet<>();

    @Relationship(type = "AFFECTS_PART", direction = Relationship.Direction.OUTGOING)
    private Set<PartNode> affectedParts = new HashSet<>();

    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.OUTGOING)
    private Set<UserNode> creators = new HashSet<>();

    public ChangeNode() {}

    public ChangeNode(String id, String title, String stage, String changeClass, String product,
                      String status, String creator, LocalDateTime createTime, String changeReason) {
        this.id = id;
        this.title = title;
        this.stage = stage;
        this.changeClass = changeClass;
        this.product = product;
        this.status = status;
        this.creator = creator;
        this.createTime = createTime;
        this.changeReason = changeReason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getChangeClass() { return changeClass; }
    public void setChangeClass(String changeClass) { this.changeClass = changeClass; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public Set<DocumentNode> getAffectedDocuments() { return affectedDocuments; }
    public void setAffectedDocuments(Set<DocumentNode> affectedDocuments) { this.affectedDocuments = affectedDocuments; }

    public Set<PartNode> getAffectedParts() { return affectedParts; }
    public void setAffectedParts(Set<PartNode> affectedParts) { this.affectedParts = affectedParts; }

    public Set<UserNode> getCreators() { return creators; }
    public void setCreators(Set<UserNode> creators) { this.creators = creators; }
}