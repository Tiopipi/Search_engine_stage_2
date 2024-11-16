package org.ulpgc.bd.control;

import java.util.List;
import java.util.logging.Logger;

public interface BookSupplier {
    String getTxtLink(String bookPageUrl, Logger logger);
    List<String> getBookPageLinks(String bookshelfNum, Logger logger);
}
