package org.ulpgc.bd.repository.interfaces;

import org.ulpgc.bd.model.Document;

public interface DocumentLoader {
    Document loadDocument(Integer documentId);
}
