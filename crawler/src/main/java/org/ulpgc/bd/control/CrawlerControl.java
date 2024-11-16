package org.ulpgc.bd.control;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CrawlerControl {

    private static final Logger logger = Logger.getLogger(CrawlerControl.class.getName());
    private final BookSupplier supplier;
    private final BookStore store;

    static {
        try {
            logger.addHandler(new FileHandler("crawler.log"));
        } catch (IOException e) {
            logger.severe("Failed to initialize logger or create directories: " + e.getMessage());
        }
    }

    public CrawlerControl(BookSupplier supplier, BookStore store) {
        this.supplier = supplier;
        this.store = store;
    }

    public int runCrawler(String bookshelf, int numBooks, int startIndex, String repository) {
        List<String> bookPageLinks = supplier.getBookPageLinks(bookshelf, logger);
        if (!bookPageLinks.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            int totalBooksDownloaded = 0;

            for (int i = startIndex; i < bookPageLinks.size(); i++) {
                String bookPageUrl = bookPageLinks.get(i);
                String bookId = bookPageUrl.substring(bookPageUrl.lastIndexOf("/") + 1);
                String txtLink = supplier.getTxtLink(bookPageUrl, logger);

                if (txtLink != null) {
                    executor.submit(() -> store.storeBook(txtLink, bookId, repository, logger));
                    totalBooksDownloaded++;
                    if (totalBooksDownloaded >= numBooks) {
                        logger.info("Downloaded " + totalBooksDownloaded + " books.");
                        executor.shutdown();
                        return i + 1;
                    }
                }
            }
            executor.shutdown();
        } else {
            logger.info("No book pages found to process.");
        }
        return -1;
    }

}
