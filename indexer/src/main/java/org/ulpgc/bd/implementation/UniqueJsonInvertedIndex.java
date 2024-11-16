package org.ulpgc.bd.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.ulpgc.bd.control.InvertedIndex;
import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndexEntry;
import java.io.*;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;


import java.util.*;

public class UniqueJsonInvertedIndex implements InvertedIndex {
    private final Analyzer analyzer = new StandardAnalyzer();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Map<String, InvertedIndexEntry> buildInvertedIndexWithPositions(List<Document> documents, Set<String> stopWords, Set<String> processedBooks, String indexDirectory) {
        Map<String, InvertedIndexEntry> index = new HashMap<>();
        for (Document document : documents) {
            String documentId = document.getId();
            if (processedBooks.contains(documentId)) {
                continue;
            }
            String content = document.getContent();
            String[] words = content.split("\\s+");
            int position = 0;
            for (String word : words) {
                String term = cleanAndNormalize(word);
                if (term != null && !stopWords.contains(term) && !term.isEmpty()) {
                    if (!index.containsKey(term)) {
                        index.put(term, new InvertedIndexEntry());
                    }
                    index.get(term).addPosition(documentId, position);
                }
                position++;
            }
        }
        return index;
    }

    @Override
    public void exportToJson(Map<String, InvertedIndexEntry> invertedIndex, String baseDirectory) {
        try {
            File outputDirectory = new File(baseDirectory);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            String outputPath = baseDirectory + "/invertedIndex.json";
            try (FileWriter fileWriter = new FileWriter(outputPath);
                 JsonWriter jsonWriter = new JsonWriter(fileWriter)) {
                jsonWriter.beginObject();
                for (Map.Entry<String, InvertedIndexEntry> entry : invertedIndex.entrySet()) {
                    jsonWriter.name(entry.getKey());
                    gson.toJson(entry.getValue(), InvertedIndexEntry.class, jsonWriter);
                }
                jsonWriter.endObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String cleanAndNormalize(String word) {
        if (word.matches(".*[^a-zA-Z0-9áéíóúàèìòùäëïöüâêîôûçñ].*")) {
            return null;
        }
        try (TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(word))) {
            tokenStream.reset();
            if (tokenStream.incrementToken()) {
                return tokenStream.getAttribute(CharTermAttribute.class).toString();
            }
            tokenStream.end();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateInvertedIndexJson(Map<String, InvertedIndexEntry> newEntries, String baseDirectory) {
        String filePath = baseDirectory + "/invertedIndex.json";
        Map<String, InvertedIndexEntry> currentIndex = new HashMap<>();
        File jsonFile = new File(filePath);
        if (jsonFile.exists()) {
            try (FileReader reader = new FileReader(jsonFile)) {
                Type type = new TypeToken<Map<String, InvertedIndexEntry>>() {}.getType();
                currentIndex = gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Map.Entry<String, InvertedIndexEntry> entry : newEntries.entrySet()) {
            String term = entry.getKey();
            InvertedIndexEntry newEntry = entry.getValue();
            currentIndex.merge(term, newEntry, (existing, toMerge) -> {
                existing.mergeWith(toMerge);
                return existing;
            });
        }
        exportToJson(currentIndex, baseDirectory);
    }
}
