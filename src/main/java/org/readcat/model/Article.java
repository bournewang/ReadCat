package org.readcat.model;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import javax.validation.constraints.Null;
import java.sql.Timestamp;
import java.sql.Date;

@Entity
@Table(name="articles")
public class Article {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    @Column(name="user_id")
    private Long userId;

    @Nullable
    @Column(name="essay_id")
    private Long essayId;

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

    @Nullable
    @Lob
    @Column(name="content", columnDefinition = "TEXT")
    private String content;

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
    public Article(Long userId, Long essayId, String title, String author, String oriUrl, String content, String highlight, Integer wordCount) {
        this.userId = userId;
        this.essayId = essayId;
        this.title = title;
        this.author = author;
        this.oriUrl = oriUrl;
//        this.url = url;
        this.content = content;
        this.highlight = highlight;
        this.wordCount = wordCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
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

    public Long getEssayId() {return essayId;}

    public void setEssayId(Long essayId) {this.essayId = essayId;}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
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
