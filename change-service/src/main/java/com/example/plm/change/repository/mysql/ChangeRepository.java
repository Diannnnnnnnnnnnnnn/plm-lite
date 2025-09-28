package com.example.plm.change.repository.mysql;

import com.example.plm.change.model.Change;
import com.example.plm.common.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChangeRepository extends JpaRepository<Change, String> {

    List<Change> findByStatus(Status status);

    List<Change> findByCreator(String creator);

    List<Change> findByProduct(String product);

    @Query("SELECT c FROM Change c WHERE c.createTime BETWEEN :startDate AND :endDate")
    List<Change> findByCreateTimeBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Change c WHERE c.title LIKE %:keyword% OR c.changeReason LIKE %:keyword%")
    List<Change> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Change c WHERE c.status = :status AND c.creator = :creator")
    List<Change> findByStatusAndCreator(@Param("status") Status status,
                                       @Param("creator") String creator);
}