package org.ulpgc.bd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InvertedIndex {
    private Map<String, DocumentData> index;

    public Map<String, DocumentData> getIndex() {
        return index;
    }

    public void setIndex(Map<String, DocumentData> index) {
        this.index = index;
    }

    public static class DocumentData {
        @JsonProperty("id")
        private List<Integer> id;

        @JsonProperty("p")
        private List<List<Integer>> p;

        @JsonProperty("f")
        private List<Integer> f;

        @JsonProperty("paragraph")
        private List<String> paragraph = new ArrayList<>();

        @JsonProperty("t")
        private List<String> title = new ArrayList<>();

        public List<Integer> getId() {
            return id;
        }

        public void setId(List<Integer> id) {
            this.id = id;
        }

        public List<List<Integer>> getP() {
            return p;
        }

        public void setP(List<List<Integer>> p) {
            this.p = p;
        }

        public List<Integer> getF() {
            return f;
        }

        public void setF(List<Integer> f) {
            this.f = f;
        }

        public List<String> getParagraph() {
            return paragraph;
        }

        public void setParagraph(List<String> paragraph) {
            this.paragraph = paragraph;
        }

        public List<String> getTitle() {
            return title;
        }

        public void setTitle(List<String> title) {
            this.title = title;
        }
    }

}
