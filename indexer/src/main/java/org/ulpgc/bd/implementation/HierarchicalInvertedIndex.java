package org.ulpgc.bd.implementation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.ulpgc.bd.control.InvertedIndex;
import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndexEntry;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class HierarchicalInvertedIndex implements InvertedIndex {
    private static final Logger logger = Logger.getLogger(org.ulpgc.bd.implementation.HierarchicalInvertedIndex.class.getName());
    private final Analyzer analyzer = new StandardAnalyzer();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void updateInvertedIndexJson(Map<String, InvertedIndexEntry> newEntries, String baseDirectory) {
        Map<String, Map<String, InvertedIndexEntry>> updatedData = new HashMap<>();
        for (Map.Entry<String, InvertedIndexEntry> entry : newEntries.entrySet()) {
            String word = entry.getKey();
            String cleanedWord = cleanAndNormalize(word);
            if (cleanedWord == null || cleanedWord.isEmpty()) continue;
            String subDir1 = cleanedWord.substring(0, 1).toLowerCase();
            String subDir2 = cleanedWord.length() > 1 ? cleanedWord.substring(0, 2).toLowerCase() : subDir1;
            String subDir3 = cleanedWord.length() > 2 ? cleanedWord.substring(0, 3).toLowerCase() : subDir2;
            updatedData.computeIfAbsent(subDir3, k -> new HashMap<>()).put(cleanedWord, entry.getValue());
        }
        for (Map.Entry<String, Map<String, InvertedIndexEntry>> letterEntry : updatedData.entrySet()) {
            String subDir3 = letterEntry.getKey();
            for (Map.Entry<String, InvertedIndexEntry> wordEntry : letterEntry.getValue().entrySet()) {
                String word = wordEntry.getKey();
                InvertedIndexEntry entryData = wordEntry.getValue();
                String subDir1 = word.substring(0, 1).toLowerCase();
                String subDir2 = word.length() > 1 ? word.substring(0, 2).toLowerCase() : subDir1;
                Path wordDirectory = Paths.get(baseDirectory, subDir1, subDir2, subDir3);
                Path filePath = wordDirectory.resolve(word + ".json");
                Map<String, InvertedIndexEntry> existingData = new HashMap<>();
                if (Files.exists(filePath)) {
                    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                        Type type = new TypeToken<Map<String, InvertedIndexEntry>>() {}.getType();
                        existingData = gson.fromJson(reader, type);
                    } catch (IOException e) {
                        logger.warning("Error reading existing index for word " + word + ": " + e.getMessage());
                    }
                }
                existingData.merge(word, entryData, (existing, newEntry) -> {
                    existing.mergeWith(newEntry);
                    return existing;
                });
                try {
                    Files.createDirectories(wordDirectory);
                    Gson gsonCompact = new Gson();
                    String jsonString = gsonCompact.toJson(existingData);
                    try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                        writer.write(jsonString);
                    }
                } catch (IOException e) {
                    logger.warning("Error updating index for word " + word + ": " + e.getMessage());
                }
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
        Gson gson = new Gson();
        for (String word : invertedIndex.keySet()) {
            String cleanedWord = cleanAndNormalize(word);
            String firstLetter = cleanedWord.substring(0, 1).toLowerCase().replaceAll("[^\\p{L}0-9]", "");
            StringBuilder pathBuilder = new StringBuilder(baseDirectory);
            pathBuilder.append('/').append(firstLetter); // Primer nivel: letra inicial
            String subDir1 = cleanedWord.length() > 1 ? cleanedWord.substring(0, 2) : cleanedWord.substring(0, 1);
            pathBuilder.append('/').append(subDir1.toLowerCase().replaceAll("[^\\p{L}0-9]", ""));
            String subDir2 = cleanedWord.length() > 2 ? cleanedWord.substring(0, 3) : subDir1;
            pathBuilder.append('/').append(subDir2.toLowerCase().replaceAll("[^\\p{L}0-9]", ""));
            File currentDir = new File(pathBuilder.toString());
            if (!currentDir.exists()) {
                currentDir.mkdirs();
            }
            InvertedIndexEntry entry = invertedIndex.get(cleanedWord);
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("id", entry.getDocIds());
            details.put("p", entry.getPositions());
            details.put("f", entry.getFrequencies());
            Map<String, Object> jsonMap = new LinkedHashMap<>();
            jsonMap.put(cleanedWord, details);
            File wordFile = new File(pathBuilder.append('/').append(cleanedWord).append(".json").toString());
            try (FileWriter writer = new FileWriter(wordFile)) {
                gson.toJson(jsonMap, writer);
            } catch (IOException e) {
                e.printStackTrace();
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
