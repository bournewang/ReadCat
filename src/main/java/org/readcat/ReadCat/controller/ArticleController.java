package org.readcat.ReadCat.controller;

import org.readcat.ReadCat.model.Article;
import org.readcat.ReadCat.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

//@CrossOrigin(origins = "*")
@RestController
public class ArticleController {
    @Autowired
    ArticleRepository repository;

    @GetMapping("/articles")
    public List<Article> index(@RequestParam(value="date", defaultValue = "") String date) throws ParseException {
        if (!StringUtils.isEmpty(date)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return repository.findAllByDate(format.parse(date));
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<Optional<Article>> get(@PathVariable Long id) {
        try {
            Optional<Article> article = repository.findById(id);
            return new ResponseEntity<Optional<Article>>(article, OK);
        }catch(NoSuchElementException e) {
            return new ResponseEntity<Optional<Article>>(NOT_FOUND);
        }
    }

    @PostMapping("/articles")
    public void add(Article article) {
        repository.save(article);
    }

    @PostMapping("/articles/{id}/content")
    public String get(@PathVariable Long id,
                      @RequestParam (value="content", defaultValue = "") String content,
                      HttpServletRequest request) {
        try {
            Article article = repository.findById(id).get();
            System.out.println("content: "+content);
            String baseUrl = request.getScheme()+"://" + request.getServerName() + ":" + request.getServerPort();
            String filename = article.getUrl().substring(baseUrl.length());
            System.out.println(article);
            System.out.println("filename: "+filename);

            String baseDir = "./";
            String uploadDir = "upload/";

            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(new File(baseDir + uploadDir + filename));
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();

            return "update content success";
        }catch(NoSuchElementException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }


    @GetMapping("/count_by_date")
    public List<Object[]> countByDate() {
        List<Object[]> res = repository.countArticlesByDate();
        res.forEach(ele -> System.out.println(ele[0] +","+ele[1]));
        return res;
    }
}