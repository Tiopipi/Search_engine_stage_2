package org.ulpgc.bd.control;

import org.ulpgc.bd.model.Document;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface BookLoader {
    List<Document> loadBooksFromDirectory(String directory, Set<String> processedBooks);
    void initializeStopWords(String stopWordsFilePath) throws IOException;
    Set<String> getStopWords();
}
