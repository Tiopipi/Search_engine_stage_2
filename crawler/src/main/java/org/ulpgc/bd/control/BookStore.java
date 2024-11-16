package org.ulpgc.bd.control;

import java.util.logging.Logger;

public interface BookStore {
    void storeBook(String urlBook, String bookId, String REPOSITORY_DOCUMENTS, Logger logger);
}
