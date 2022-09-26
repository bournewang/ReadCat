package org.readcat.controller;

import org.readcat.model.Article;
import org.readcat.model.Essay;
import org.readcat.repository.ArticleRepository;
import org.readcat.security.model.UserShow;
import org.readcat.utils.Common;
import org.readcat.utils.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
    public Page<Article> index(@RequestParam(value="date", defaultValue = "") String date,
                               @RequestParam(value="page", defaultValue = "0") Integer page
                               ) throws ParseException {

        UserShow user = Common.getCurrentUser();
        System.out.println("====== current user");
        System.out.println(user);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);

        if (!StringUtils.isEmpty(date)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return repository.findAllByDate(user.getId(), format.parse(date), pageable);
        }
        return repository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
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

    @GetMapping("/articles/findByEssayId/{essayId}")
    public ResponseEntity<Optional<Article>> findByEssayId(@PathVariable Long essayId) {
        try {
            UserShow user = Common.getCurrentUser();
            Optional<Article> article = repository.findFirstByUserIdAndEssayIdOrderByCreatedAtDesc(user.getId(), essayId);
            return new ResponseEntity<Optional<Article>>(article, OK);
        }catch(NoSuchElementException e) {
            return new ResponseEntity<Optional<Article>>(NOT_FOUND);
        }
    }

    @PostMapping("/articles/{id}")
    public Long update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        repository.save(article);
        return id;
    }

//    delete articles by id
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Optional<Article>> delete(@PathVariable Long id) {
        try {
            Optional<Article> article = repository.findById(id);
            repository.delete(article.get());
            return new ResponseEntity<Optional<Article>>(OK);
        }catch(NoSuchElementException e) {
            return new ResponseEntity<Optional<Article>>(NOT_FOUND);
        }
    }

    @PostMapping("/articles")
    public Long create(@RequestBody Article article, HttpServletRequest request) throws Exception {
        System.out.println(article);
        UserShow user = Common.getCurrentUser();
        String content = article.getContent();
        String filename = Md5Utils.md5(content) + ".html";

//       calculate words number in content, and set to wordCount
        article.setWordCount(content.split(" ").length);
//        for every line in content, add <p> tag
        String[] lines = content.split("\n");
        String contentWithP = "";
        for (String line : lines) {
            contentWithP += "<p>" + line + "</p>";
        }
        article.setContent(contentWithP);
        article.setUserId(user.getId());
        article.setOriUrl(filename);
        article.setUrl("-");

//        save article content
        repository.save(article);

//        Optional<Article> article = repository.findById(id);
        return article.getId();
    }

    @PostMapping("/articles-from-essay")
    public ResponseEntity<Article> createFromEssay(@RequestBody Essay essay, HttpServletRequest request) throws Exception {
        System.out.println(essay);
        UserShow user = Common.getCurrentUser();
        Article article = new Article(
                user.getId(),
                essay.getId(),
                essay.getTitle(),
                essay.getAuthor(),
                essay.getOriUrl(),
                essay.getContent(),
                "",
                essay.getWordCount());
        repository.save(article);

        return new ResponseEntity<Article>(article, OK);
    }

    @GetMapping("/get-all-read-essays")
    public List<Long> getAllReadEssays() {
        UserShow user = Common.getCurrentUser();
        List<Long> res = repository.findAllEssayByUserId(user.getId());
//        filter res without null
        res.removeIf(item -> item == null);
        res.forEach(ele -> System.out.println(ele));
        return res;
    }

    @GetMapping("/articles/update/contents")
    public String updateContent(HttpServletRequest request)
    {
        //		for every article in the database, update the content from the url
        UserShow user = Common.getCurrentUser();
        List<Article> articles = repository.findAllByUserId(user.getId());
        System.out.println("get articles count: "+articles.size());
        for (Article article : articles) {
            String baseUrl = request.getScheme()+"://" + request.getServerName() + ":" + request.getServerPort();
            String filename = article.getUrl().substring(baseUrl.length());
            String content = Common.getFile("./upload"+filename);
//            String content = Common.getFile("./upload/"+article.getOriUrl());
            article.setContent(content);
            System.out.println("update content of "+article.getId() +", "+article.getTitle()+", content length: "+content.length());
            try {
                repository.save(article);
            }catch (Exception e) {
                System.out.println("save article error: "+e.getMessage());
            }
        }

        return "success update content of articles";
    }

}