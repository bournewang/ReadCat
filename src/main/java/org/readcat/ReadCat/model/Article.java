package org.readcat.ReadCat.model;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.sql.Timestamp;
import java.sql.Date;

@Entity
@Table(name="articles")
public class Article {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @NotNull
    @Column(name="user_id")
    private long userId;

    @Nullable
    @Column(name="title")
    private String title;

    @Nullable
    @Column(name="author")
    private String author;

    @NotNull
    @Column(name="ori_url")
    private String oriUrl;

    @NotNull
    @Column(name="url")
    private String url;

    @NotNull
    @Column(name="highlight")
    private String highlight;

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    @Nullable
    @Column(name="word_count")
    private Integer wordCount;

    @NotNull
    @Column(name="created_at", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable = false,updatable = false)
    @Generated(GenerationTime.INSERT)
    private Date createdAt;

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", oriUrl='" + oriUrl + '\'' +
                ", url='" + url + '\'' +
                ", highlight='" + highlight + '\'' +
                ", wordCount='" + wordCount + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public Article(){}
    public Article(long userId, String title, String author, String oriUrl, String url, String highlight, Integer wordCount) {
        this.userId = userId;
        this.title = title;
        this.author = author;
        this.oriUrl = oriUrl;
        this.url = url;
        this.highlight = highlight;
        this.wordCount = wordCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getOriUrl() {
        return oriUrl;
    }

    public void setOriUrl(String oriUrl) {
        this.oriUrl = oriUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
