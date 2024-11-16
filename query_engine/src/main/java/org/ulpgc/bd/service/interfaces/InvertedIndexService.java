package org.ulpgc.bd.service.interfaces;

import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndex;
import java.util.Map;


public interface InvertedIndexService {
    Map<String, InvertedIndex.DocumentData> searchInvertedIndex(String query, Map<String, InvertedIndex> loadedIndexes, Map<Integer, Document> documentMap);
}
