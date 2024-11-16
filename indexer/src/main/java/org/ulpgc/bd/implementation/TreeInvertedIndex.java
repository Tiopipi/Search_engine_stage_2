package org.ulpgc.bd.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.ulpgc.bd.control.InvertedIndex;
import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndexEntry;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class TreeInvertedIndex implements InvertedIndex {
    private static final Logger logger = Logger.getLogger(org.ulpgc.bd.implementation.TreeInvertedIndex.class.getName());
    private static final StandardAnalyzer analyzer = new StandardAnalyzer();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void updateInvertedIndexJson(Map<String, InvertedIndexEntry> newEntries, String baseDirectory) {
        Map<String, Map<String, InvertedIndexEntry>> updatedData = new HashMap<>();
        for (Map.Entry<String, InvertedIndexEntry> entry : newEntries.entrySet()) {
            String word = entry.getKey();
            String firstLetter = word.substring(0, 1).toLowerCase().replaceAll("[^a-z0-9áéíóúàèìòùäëïöüâêîôûçñ]", "");
            if (!firstLetter.isEmpty()) {
                updatedData.computeIfAbsent(firstLetter, k -> new HashMap<>()).put(word, entry.getValue());
            }
        }
        for (Map.Entry<String, Map<String, InvertedIndexEntry>> letterEntry : updatedData.entrySet()) {
            String letter = letterEntry.getKey();
            Path letterDirectory = Paths.get(baseDirectory, letter);
            Path filePath = letterDirectory.resolve(letter + "_words.json");
            Map<String, InvertedIndexEntry> existingData = new HashMap<>();
            if (Files.exists(filePath)) {
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    Type type = new TypeToken<Map<String, InvertedIndexEntry>>() {}.getType();
                    existingData = gson.fromJson(reader, type);
                } catch (IOException e) {
                    logger.warning("Error reading existing index for letter " + letter + ": " + e.getMessage());
                }
            }
            for (Map.Entry<String, InvertedIndexEntry> entry : letterEntry.getValue().entrySet()) {
                existingData.merge(entry.getKey(), entry.getValue(), (existing, newEntry) -> {
                    existing.mergeWith(newEntry);
                    return existing;
                });
            }
            Gson gsonCompact = new Gson();
            String jsonString = gsonCompact.toJson(existingData);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write(jsonString);
            } catch (IOException e) {
                logger.warning("Error updating index for letter " + letter + ": " + e.getMessage());
            }
        }
    }


    @Override
    public Map<String, InvertedIndexEntry> buildInvertedIndexWithPositions(List<Document> documents, Set<String> stopWords, Set<String> processedBooks, String indexDirectory) {
        Map<String, InvertedIndexEntry> index = new HashMap<>();
        for (Document document : documents) {
            String documentId = document.getId();
            if (processedBooks.contains(documentId)) {
                System.out.println("The document " + documentId + " has already been processed. Omitting...");
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
        Map<String, Map<String, InvertedIndexEntry>> letterData = new HashMap<>();
        for (Map.Entry<String, InvertedIndexEntry> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            String firstLetter = word.substring(0, 1).toLowerCase().replaceAll("[^a-z0-9áéíóúàèìòùäëïöüâêîôûçñ]", "");
            if (!firstLetter.isEmpty()) {
                letterData.computeIfAbsent(firstLetter, k -> new HashMap<>()).put(word, entry.getValue());
            }
        }
        for (Map.Entry<String, Map<String, InvertedIndexEntry>> letterEntry : letterData.entrySet()) {
            String letter = letterEntry.getKey();
            Path letterDirectory = Paths.get(baseDirectory, letter);
            try {
                Files.createDirectories(letterDirectory);
                Path outputFile = letterDirectory.resolve(letter + "_words.json");
                Gson gsonCompact = new Gson();
                String jsonString = gsonCompact.toJson(letterEntry.getValue());
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
                    writer.write(jsonString);
                }
            } catch (IOException e) {
                logger.warning("Error exporting index for letter " + letter + ": " + e.getMessage());
            }
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
}
