package org.delcom.app.repositories;

import org.delcom.app.entities.PlantHealth;
import org.delcom.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlantHealthRepository extends JpaRepository<PlantHealth, Long> {
    List<PlantHealth> findByUser(User user);
}