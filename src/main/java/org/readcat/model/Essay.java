package org.readcat.model;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name="essays")
public class Essay {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Nullable
    @Column(name="title")
    private String title;

    @Nullable
    @Column(name="category_id")
    private long categoryId;

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @Nullable
    @Column(name="author")
    private String author;

    @NotNull
    @Column(name="ori_url")
    private String oriUrl;

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

    public Essay() {
    }

    @Override
    public String toString() {
        return "Essay{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", categoryId=" + categoryId +
                ", author='" + author + '\'' +
                ", oriUrl='" + oriUrl + '\'' +
                ", wordCount=" + wordCount +
                ", createdAt=" + createdAt +
                '}';
    }

    public Essay(Long categoryId, String title, String author, String oriUrl, String content, Integer wordCount) {
        this.categoryId = categoryId;
        this.title = title;
        this.author = author;
        this.oriUrl = oriUrl;
        this.content = content;
        this.wordCount = wordCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
