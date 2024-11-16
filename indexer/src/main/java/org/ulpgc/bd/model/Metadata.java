package org.ulpgc.bd.model;

public class Metadata {
    private final String title;
    private final String author;
    private final String releaseDate;
    private final String language;
    private final String id;

    public Metadata(String title, String author, String releaseDate, String language, String id) {
        this.title = title;
        this.author = author;
        this.releaseDate = releaseDate;
        this.language = language;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getLanguage() {
        return language;
    }

    public String getId() {
        return id;
    }
}
