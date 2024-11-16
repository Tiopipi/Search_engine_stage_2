package org.ulpgc.bd.apps;

import org.ulpgc.bd.control.CrawlerControl;
import org.ulpgc.bd.implementation.GutenbergSupplier;
import org.ulpgc.bd.implementation.TextFormatStore;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int MAX_BOOKSHELF = 488;
    private static int bookshelfNum = 5;
    private static int startIndex = 0;

    public static void main(String[] args) {
        String REPOSITORY_DOCUMENTS = System.getenv("REPOSITORY_DOCUMENTS");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        TextFormatStore textFormatStore =new TextFormatStore();
        GutenbergSupplier gutenbergSupplier = new GutenbergSupplier();
        CrawlerControl crawlerControl = new CrawlerControl(gutenbergSupplier, textFormatStore);


        scheduler.scheduleAtFixedRate(() -> {
            if (bookshelfNum >= MAX_BOOKSHELF) {
                logger.info("There are no more libraries left to process. Ending execution");
                scheduler.shutdown();
            } else {
                logger.info("Starting crawler for bookshelf number: " + bookshelfNum);
                startIndex = crawlerControl.runCrawler(String.valueOf(bookshelfNum), 50, startIndex, REPOSITORY_DOCUMENTS);
                if (startIndex == -1) {
                    bookshelfNum++;
                    startIndex = 0;
                }
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

}
