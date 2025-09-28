package com.example.document_service.client;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.plm.common.model.Status;
import com.example.plm.common.model.Stage;

@FeignClient(name = "search-service")
public interface SearchServiceClient {

    @PostMapping("/search/index")
    void indexDocument(@RequestBody DocumentEsDto doc);

    @GetMapping("/search")
    List<DocumentEsDto> search(@RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "stage", required = false) String stage,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "category", required = false) String category);

    // Lightweight ES DTO carried over the wire
    class DocumentEsDto {
        private String id;
        private String masterId;
        private String title;
        private String category;
        private Stage stage;
        private Status status;
        private String creator;
        private LocalDateTime createTime;

        public DocumentEsDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getMasterId() { return masterId; }
        public void setMasterId(String masterId) { this.masterId = masterId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Stage getStage() { return stage; }
        public void setStage(Stage stage) { this.stage = stage; }

        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }

        public String getCreator() { return creator; }
        public void setCreator(String creator) { this.creator = creator; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}
