package org.ulpgc.bd.repository.interfaces;

import org.ulpgc.bd.model.InvertedIndex;

public interface InvertedIndexLoader {
    InvertedIndex loadInvertedIndex(String query);
}
