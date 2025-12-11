package org.delcom.app.repositories;

import org.delcom.app.entities.Plant;
import org.delcom.app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlantRepository extends JpaRepository<Plant, UUID> {
    List<Plant> findByUser(User user);
}