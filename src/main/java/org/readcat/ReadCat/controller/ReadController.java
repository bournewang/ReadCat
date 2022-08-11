package org.readcat.ReadCat.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.readcat.ReadCat.model.Article;
import org.readcat.ReadCat.model.UpdateArticle;
import org.readcat.ReadCat.model.User;
import org.readcat.ReadCat.repository.ArticleRepository;
import org.readcat.ReadCat.utils.Md5Utils;
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
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Object[]> res = repository.countArticlesByDate(user.getId());
        res.forEach(ele -> System.out.println(ele[0] +","+ele[1]));
        return res;
    }

    @PostMapping("/parse")
    public ResponseEntity<Optional<Article>> parse(@RequestParam(value = "url", defaultValue = "") String url, HttpServletRequest request) {
        Document doc = null;
        try {
            System.out.println("parse "+url);
            doc = Jsoup.connect(url).get();
            System.out.println("get doc ");
            Element body = doc.body();
            Matcher m = Pattern.compile("(\\S+)").matcher(body.text());

//            total words in this page
            long numWordsOnPage = m.results().count();
            Elements ps = body.select("p");
            System.out.println(doc.title());
            System.out.println("total words: " + numWordsOnPage);

            Element pWithMostWords = body;
            long highestWordCount = 0;

            if (ps.size() == 0) {
                ps = body.select("div");
            }

//            find the paragraph with most words
            for (Element p : ps) {
                Matcher m2 = Pattern.compile("(\\S+)").matcher(p.text());
                long wordCount = m2.results().count();// p.innerText.match(/\S+/g);
                System.out.println("word count: " + wordCount);
                if (wordCount > highestWordCount) {
                    highestWordCount = wordCount;
                    pWithMostWords = p;
                    System.out.println("highestWordCount: " + wordCount);
                }
            }

            // Keep selecting more generally until over 2/5th of the words on the page have been selected
            Element selectedContainer = pWithMostWords;
            long wordCountSelected = highestWordCount;

//            if the paragraph has words less than 40% of the total words
//            get the parent node
            while (((float) wordCountSelected / numWordsOnPage < 0.4)
                    && !selectedContainer.equals(body)
                    && selectedContainer.parent().text().length() > 0) {
                selectedContainer = selectedContainer.parent();
                Matcher m3 = Pattern.compile("(\\S+)").matcher(selectedContainer.text());
                wordCountSelected = m3.results().count();
                System.out.println("selected/totalwords < 0.4, get parent, wordCountSelected count: " + wordCountSelected);
            }

            // Make sure a single p tag is not selected
            if (selectedContainer.tagName() == "P") {
                selectedContainer = selectedContainer.parent();
            }
//            return selectedContainer;
//            fetch base url
            URL urlObj = new URL(url);
            String oriBase = urlObj.getProtocol() + "://" + urlObj.getHost();
            Integer port = urlObj.getPort();
            if (port > 0 && port != 80 && port != 443) oriBase += ":" + port.toString();

            for (Element img: selectedContainer.select("img")) {
                if (!img.attr("data-src").isEmpty()) {
                    img.attr("src", img.attr("data-src"));
                }
                img.setBaseUri(oriBase);
                System.out.println("set baseurl "+oriBase+" to img "+img.attr("src"));
            }

            String filename = Md5Utils.md5(url) + ".html";

            String relPath = writeToLocal(filename, selectedContainer.outerHtml());
            System.out.println("relpath: "+relPath);
            String[] words = selectedContainer.text().split("\\S+");

            String baseUrl = request.getScheme()+"://" +
                    request.getServerName() + ":" + request.getServerPort();
            System.out.println("base url: " + baseUrl);
            String localUrl = baseUrl + '/' + relPath;
            System.out.println("localUrl: " + localUrl);

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Article article = new Article(
                    user.getId(),
                    getTitle(doc),
                    "",
                    url,
                    localUrl,
                    "",
                    words.length);
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

    private String getTitle(Document doc) {
        Elements elements = doc.head().select("title");
        if (elements.isEmpty()) return null;

        String title = elements.first().text();

        String[] seperators = {"-", "–", "|", ":"};
        // Get the part before the first — if it exists
        for (String s : seperators) {
            s = " " + s + " ";
            if (title.contains(s)) {
                return title.substring(0, title.indexOf(s));
            }
        }
        for (String s : seperators) {
            if (title.contains(s)) {
                return title.substring(0, title.indexOf(s));
            }
        }

        return title;
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
