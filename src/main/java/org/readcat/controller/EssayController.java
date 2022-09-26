package org.readcat.controller;

import io.micronaut.http.annotation.Get;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.readcat.model.Category;
import org.readcat.model.Essay;
import org.readcat.repository.ArticleRepository;
import org.readcat.repository.CategoryRepository;
import org.readcat.repository.EssayRepository;
import org.readcat.security.model.UserShow;
import org.readcat.utils.Common;
import org.readcat.utils.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequestMapping("/api")
@RestController
public class EssayController {
    @Autowired
    EssayRepository repository;
    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping("/crawl-websites")
    public String crawlWebsites(@RequestParam(value="category", defaultValue = "") String cat,
                                @RequestParam(value="url", defaultValue = "") String website){
//        init a hashMap with initial values "hello":"xxxxx"
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("Telegraph", "https://www.telegraph.co.uk");
            put("Mirror", "https://www.mirror.co.uk");
            put("Metro", "https://metro.co.uk");
            put("Standard", "https://www.standard.co.uk");
            put("Politics", "https://www.politics.co.uk");
            put("Wired", "https://www.wired.co.uk");
            put("NationalGeographic", "https://www.nationalgeographic.com/");
            put("Economist", "https://www.economist.com");
            put("NewScientist", "https://www.newscientist.com");
        }};

//        for map, get key and value
//        for (String cat : map.keySet()) {
        if (website.isEmpty() && !cat.isEmpty())
            website = map.get(cat);
            System.out.println("Key = " + cat + ", Value = " + website);

            Optional<Category> c = categoryRepository.findByName(cat);
            Category category = null;
            if (c.isEmpty()) {
                category = new Category(cat);
//                category.setName(cat);
                categoryRepository.save(category);
            }else{
                category = c.get();
            }

            Document doc = null;
            try {
                doc = Jsoup.connect(website).get();
            }catch(IOException e){
                System.out.println("error: " + e.getMessage());
                return "failed";
            }
            Elements links = doc.select("a");
            System.out.println("===== processing " + website);
            for (Element link : links) {
                System.out.println("link: " + link.attr("href"));
//				if link href not start with http, add website to link
                String linkHref = link.attr("href");
//                skip for external links
                if (!linkHref.startsWith("http")) {
                    linkHref = website + linkHref;
                }
//                if (!linkHref.startsWith(website)){
//                    System.out.println("skip external link");
//                    continue;
//                }
                //                skip if link length less than 10
                if (linkHref.length() < (website.length() + 30)) {
                    System.out.println("skip short link(length < 30)");
                    continue;
                }
//                System.out.println("linkHref: " + linkHref);
                HashMap<String, Object> res = null;
                try {
                    res = Common.getTitleContentFromUrl(linkHref);
                    if (res == null) continue;

                    String title = (String) res.get("title");
                    String content = (String) res.get("content");
                    Integer wordCount = (Integer) res.get("wordCount");

                    if (wordCount < 400) {
                        System.out.println("word count " +wordCount+"< 400, continue;");
                    }

    //				if essay not exists with oriUrl=linkHref, create it
                    Optional<Essay> e = repository.findFirstByOriUrl(linkHref);
                    if (!e.isEmpty()) {
                        System.out.println("essay with already exists, continue");
                        continue;
                    }

                    Essay essay = new Essay(
                            category.getId(),
                            title,
                            null,
                            linkHref,
                            content,
                            wordCount
                    );
                    repository.save(essay);
                    System.out.println("save essay "+essay);
                }catch (Exception e){
                    System.out.println("error: " + e.getMessage());
                    continue;
                }
            }
//        }
        return "success";
    }
    @GetMapping("/essays-count-by-category")
    public List<Object[]> countByCategory() {
        UserShow user = (UserShow) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Object[]> res = repository.countByCategory();
        res.forEach(ele -> System.out.println(ele[0] +","+ele[1]));
        return res;
    }

    @GetMapping("/essays")
    public Page<Essay> index(@RequestParam(value="category_id", defaultValue = "0") Long category_id,
                             @RequestParam(value="page", defaultValue = "0") Integer page,
                             HttpServletRequest request) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(page, 10, sort);

        if (category_id > 0) {
            return repository.findAllByCategoryIdOrderByIdDesc(category_id, pageable);
        }
        return repository.findAll(pageable);
    }

    @GetMapping("/essays/{id}")
    public Essay get(@PathVariable Long id) {
        return repository.findById(id).get();
    }

    @DeleteMapping("/essays/{id}")
    public Long delete(@PathVariable Long id) {
        repository.deleteById(id);
        return id;
    }

    @PostMapping("/essays/{id}")
    public Long update(@PathVariable Long id, HttpServletRequest request) {
        Essay essay = repository.findById(id).get();
        essay.setTitle(request.getParameter("title"));
        essay.setContent(request.getParameter("content"));
        repository.save(essay);
        return id;
    }

    @PostMapping("/essays/create")
    public Long create(HttpServletRequest request) {
        Essay essay = new Essay();
        essay.setTitle(request.getParameter("title"));
        essay.setContent(request.getParameter("content"));
//        essay.setUserId(Common.getCurrentUser().getId());
        repository.save(essay);
        return essay.getId();
    }

    @GetMapping("/essays/insert/data")
    public void insertData(HttpServletRequest request) {
// 打开目录"./data", 读取每一个文件名和内容
        File dir = new File("./data");
        File[] files = dir.listFiles();
        for (File file : files) {
//            get filename without extension
            String fileName = file.getName();
            String title = fileName.substring(0, fileName.lastIndexOf("."));
            String content = Common.getFile(file.getAbsolutePath());
            Essay essay = new Essay();
            essay.setTitle(title);
            essay.setContent(content);
            try {
                essay.setOriUrl(Md5Utils.md5(content));
            }catch (Exception e) {
                e.printStackTrace();
            }
            essay.setWordCount(content.split(" ").length);

            System.out.println("insert essay "+title);
            repository.save(essay);
        }
    }


}
