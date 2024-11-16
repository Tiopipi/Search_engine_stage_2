package org.ulpgc.bd.model;

public class Document {
    private final String id;
    private final String content;

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}

