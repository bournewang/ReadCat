package org.readcat.model;

public class UpdateArticle {
    private String url;
    private String content;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UpdateArticle(String url, String content) {
        this.url = url;
        this.content = content;
    }
}

