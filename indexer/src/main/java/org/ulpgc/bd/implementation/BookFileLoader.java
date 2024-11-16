package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.BookLoader;
import org.ulpgc.bd.model.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;

public class BookFileLoader implements BookLoader {
    private static final Logger logger = Logger.getLogger(org.ulpgc.bd.implementation.BookFileLoader.class.getName());
    private static final Set<String> stopWords = new HashSet<>();

    @Override
    public List<Document> loadBooksFromDirectory(String directory, Set<String> processedBooks) {
        List<Document> documents = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory), "*.txt")) {
            for (Path entry : stream) {
                String bookFileName = entry.getFileName().toString();
                if (!processedBooks.contains(bookFileName)) {
                    String content = Files.readString(entry);
                    String bookContent = extractBookContent(content);
                    if (bookContent != null) {
                        documents.add(new Document(bookFileName, bookContent));
                    }
                }
            }
        } catch (IOException e) {
            logger.warning("Error loading books: " + e.getMessage());
        }
        return documents;
    }

    private String extractBookContent(String content) {
        Pattern pattern = Pattern.compile("\\*\\*\\* START OF .* \\*\\*\\*");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return content.substring(matcher.end()).trim();
        }
        return null;
    }

    @Override
    public void initializeStopWords(String stopWordsFilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(stopWordsFilePath)), StandardCharsets.UTF_8);
        content = content.replaceAll("[{}\\s]", "");
        String[] words = content.split(",");
        for (String word : words) {
            word = word.replace("'", "").toLowerCase().trim();
            if (!word.isEmpty()) {
                stopWords.add(word);
            }
        }
    }

    @Override
    public Set<String> getStopWords() {
        return stopWords;
    }
}

