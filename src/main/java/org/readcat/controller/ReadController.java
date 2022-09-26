package org.readcat.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.readcat.model.Article;
import org.readcat.model.UpdateArticle;
import org.readcat.repository.ArticleRepository;
import org.readcat.security.model.User;
import org.readcat.security.model.UserShow;
import org.readcat.utils.Common;
import org.readcat.utils.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/api/read")
@RestController
public class ReadController {
    @Autowired
    ArticleRepository repository;

    @GetMapping("/count_by_date")
    public List<Object[]> countByDate() {
        UserShow user = (UserShow) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Object[]> res = repository.countArticlesByDate(user.getId());
        res.forEach(ele -> System.out.println(ele[0] +","+ele[1]));
        return res;
    }

    @PostMapping("/parse")
    public ResponseEntity<Optional<Article>> parse(@RequestParam(value = "url", defaultValue = "") String url, HttpServletRequest request) {
        Document doc = null;
        try {
            HashMap<String, Object> res = Common.getTitleContentFromUrl(url);

            UserShow user = Common.getCurrentUser();
            Article article = new Article(
                    user.getId(),
                    null,
                    (String) res.get("title"),
                    "",
                    url,
                    (String) res.get("content"),
                    "",
                    (Integer) res.get("wordCount"));
            repository.save(article);

            return new ResponseEntity<Optional<Article>>(Optional.of(article), HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<Optional<Article>>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String writeToLocal(String filename, String html) throws IOException {
//        String baseDir = "src/main/resources/static/";
        String baseDir = "./";
        String uploadDir = "upload/";
        File directories = new File(baseDir + uploadDir);
        if (directories.exists()) {
            System.out.println("文件上传根目录已存在");
        } else { // 如果目录不存在就创建目录
            if (directories.mkdirs()) {
                System.out.println("创建多级目录成功");
            } else {
                System.out.println("创建多级目录失败");
            }
        }

        String relPath = uploadDir + filename;
        FileOutputStream fileOutputStream;
        fileOutputStream = new FileOutputStream(new File(baseDir + relPath));
        fileOutputStream.write(html.getBytes());
        fileOutputStream.close();
        System.out.println("relpath: "+filename);
        return filename;
    }



    @PostMapping("/update")
    public String get(@RequestBody UpdateArticle article,
                      HttpServletRequest request) {
        try {
//            Article article = repository.findById(id).get();
//            System.out.println("content: "+content);
            String baseUrl = request.getScheme()+"://" + request.getServerName() + ":" + request.getServerPort();
            String filename = article.getUrl().substring(baseUrl.length());
//            System.out.println(article);
            System.out.println("filename: "+filename);

            String baseDir = "./";
            String uploadDir = "upload/";

            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(new File(baseDir + uploadDir + filename));
            fileOutputStream.write(article.getContent().getBytes());
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
}
