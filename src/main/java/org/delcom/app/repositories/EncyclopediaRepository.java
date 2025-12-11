package org.delcom.app.repositories;

import org.delcom.app.entities.Encyclopedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EncyclopediaRepository extends JpaRepository<Encyclopedia, Long> {
    // Kosong saja, Spring Boot otomatis menyediakan fungsi database
}
