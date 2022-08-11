package org.readcat.ReadCat.controller;

import org.readcat.ReadCat.model.Article;
import org.readcat.ReadCat.model.User;
import org.readcat.ReadCat.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api")
@RestController
public class ArticleController {
    @Autowired
    ArticleRepository repository;

    @GetMapping("/articles")
    public List<Article> index(@RequestParam(value="date", defaultValue = "") String date) throws ParseException {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("====== current user");
        System.out.println(user);
        if (!StringUtils.isEmpty(date)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return repository.findAllByDate(user.getId(), format.parse(date));
        }
        return repository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
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
    public void create(Article article) {
        repository.save(article);
    }



}