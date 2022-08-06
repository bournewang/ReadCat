package org.readcat.ReadCat;

import org.readcat.ReadCat.model.Article;
import org.readcat.ReadCat.repository.ArticleRepository;
import org.readcat.ReadCat.repository.UserRepository;
import org.readcat.ReadCat.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@ComponentScan(basePackages = {"org.readcat.ReadCat"})
public class UserApp implements CommandLineRunner {
    @Autowired
    UserRepository repository;
    @Autowired
    ArticleRepository articleRepository;

    public static void main(String[] args) {
        SpringApplication.run(UserApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        repository.save(new User("bourne@163.com", "123456"));
//        repository.save(new User("marry@gmail.com", "123456"));
//        Iterable<User> users= repository.findAll();
//        users.forEach(user -> System.out.println(user.toString()));
        List<Article> articles = articleRepository.findAll();
        articles.forEach(article -> System.out.println(article));
    }
}
