package org.readcat.repository;

import org.readcat.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

//    List<Article> findAllOrderByCreatedAtBetween
    List<Article> findAllByUserId(@Param("userId") long userId);
    Page<Article> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") long userId, Pageable pageable);

    //    @Query("from Article a order by a.createdAt desc")
//    List<Article> findAllByCreatedAtBetween(Date start, Date end);

    @Query("from Article a where a.userId = :userId and date(a.createdAt) = :d order by a.createdAt desc")
    Page<Article> findAllByDate(@Param("userId") long userId, @Param("d") Date d, Pageable pageable);

//    @Query("SELECT c.year, COUNT(c.year) FROM Comment AS c GROUP BY c.year ORDER BY c.year DESC")
    @Query("SELECT date(a.createdAt) as createDate, COUNT(a) as total FROM Article a where a.userId = :userId GROUP BY createDate order by createDate desc")
    List<Object[]> countArticlesByDate(@Param("userId") long userId);

    @Query("select distinct(a.essayId) from Article a where a.userId = :userId")
    List<Long> findAllEssayByUserId(@Param("userId") long userId);

    Optional<Article> findFirstByUserIdAndEssayIdOrderByCreatedAtDesc(@Param("userId") long userId, @Param("essayId") long essayId);

}
