package org.delcom.todos.repositories; 

import org.delcom.todos.entities.CashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, UUID> {
    
    // Wajib: findByKeyword untuk pencarian (sesuai Test TA)
    @Query("SELECT c FROM CashFlow c WHERE " + 
           "LOWER(c.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.source) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.label) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CashFlow> findByKeyword(String keyword);

    // Wajib: findDistinctLabels (sesuai Test TA)
    @Query("SELECT DISTINCT c.label FROM CashFlow c")
    List<String> findDistinctLabels();
}