package org.ulpgc.bd.implementation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ulpgc.bd.control.BookSupplier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GutenbergSupplier implements BookSupplier {

    private static final String BASE_URL = "https://www.gutenberg.org/";

    @Override
    public List<String> getBookPageLinks(String bookshelfNum, Logger logger) {
        String categoryUrl = BASE_URL + "ebooks/bookshelf/" + bookshelfNum;
        List<String> bookPageLinks = new ArrayList<>();
        String currentPage = categoryUrl;

        while (currentPage != null) {
            try {
                Document doc = Jsoup.connect(currentPage).get();
                Elements links = doc.select("a[href]");

                for (Element link : links) {
                    String href = link.attr("href");
                    if (href.startsWith("/ebooks/") && href.substring(8).matches("\\d+")) {
                        bookPageLinks.add(BASE_URL + href);
                    }
                }

                Element nextButton = doc.select("a:contains(Next)").first();
                currentPage = (nextButton != null) ? BASE_URL + nextButton.attr("href") : null;
            } catch (IOException e) {
                logger.warning("Error getting book pages: " + e.getMessage());
                break;
            }
        }

        logger.info("Links to book pages found: " + bookPageLinks.size());
        return bookPageLinks;
    }

    @Override
    public String getTxtLink(String bookPageUrl, Logger logger) {
        try {
            String bookId = bookPageUrl.substring(bookPageUrl.lastIndexOf("/") + 1);
            String txtUrl = "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".txt";

            HttpURLConnection connection = (HttpURLConnection) new URL(txtUrl).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK ? txtUrl : null;
        } catch (IOException e) {
            logger.warning("Error getting text link: " + e.getMessage());
            return null;
        }
    }

}
