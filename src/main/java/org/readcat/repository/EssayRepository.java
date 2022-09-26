package org.readcat.repository;

import org.readcat.model.Article;
import org.readcat.model.Essay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EssayRepository extends JpaRepository<Essay, Long> {
    @Override
    Page<Essay> findAll(Pageable pageable);

//    <Essay> findAll();

//    @Override
    Optional<Essay> findFirstByOriUrl(@Param("oriUrl") String oriUrl);

    Page<Essay> findAllByCategoryIdOrderByIdDesc(@Param("categoryId") long categoryId, Pageable page);

//    get essays count by category
    @Query("SELECT e.categoryId, COUNT(e) as total, c.name FROM Essay e INNER JOIN Category c on e.categoryId = c.id GROUP BY e.categoryId")
    List<Object[]> countByCategory();

}
