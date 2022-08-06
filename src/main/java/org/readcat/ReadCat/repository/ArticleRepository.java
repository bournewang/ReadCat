package org.readcat.ReadCat.repository;

import org.readcat.ReadCat.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.Tuple;
import java.util.Date;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

//    List<Article> findAllOrderByCreatedAtBetween
    List<Article> findAllByOrderByCreatedAtDesc();

    @Query("from Article a order by a.createdAt desc")
    List<Article> findAllByCreatedAtBetween(Date start, Date end);

    @Query("from Article a where date(a.createdAt) = :d order by a.createdAt desc")
    List<Article> findAllByDate(@Param("d") Date d);

//    @Query("SELECT c.year, COUNT(c.year) FROM Comment AS c GROUP BY c.year ORDER BY c.year DESC")
    @Query("SELECT date(a.createdAt) as createDate, COUNT(a) as total FROM Article a GROUP BY createDate order by createDate desc")
    List<Object[]> countArticlesByDate();

}
