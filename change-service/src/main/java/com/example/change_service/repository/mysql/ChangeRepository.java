package com.example.change_service.repository.mysql;

import com.example.change_service.model.Changes;
import com.example.plm.common.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChangeRepository extends JpaRepository<Changes, String> {

    List<Changes> findByStatus(Status status);

    List<Changes> findByCreator(String creator);

    List<Changes> findByProduct(String product);

    @Query("SELECT c FROM Changes c WHERE c.createTime BETWEEN :startDate AND :endDate")
    List<Changes> findByCreateTimeBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Changes c WHERE c.title LIKE %:keyword% OR c.changeReason LIKE %:keyword%")
    List<Changes> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Changes c WHERE c.status = :status AND c.creator = :creator")
    List<Changes> findByStatusAndCreator(@Param("status") Status status,
                                       @Param("creator") String creator);
}

