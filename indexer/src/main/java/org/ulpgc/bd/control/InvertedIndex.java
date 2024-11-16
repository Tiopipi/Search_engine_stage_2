package org.ulpgc.bd.control;

import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndexEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InvertedIndex {

    Map<String, InvertedIndexEntry> buildInvertedIndexWithPositions(List<Document> documents, Set<String> stopWords, Set<String> processedBooks, String indexDirectory);

    void updateInvertedIndexJson(Map<String, InvertedIndexEntry> newEntries, String baseDirectory);

    void exportToJson(Map<String, InvertedIndexEntry> invertedIndex, String baseDirectory);

}
