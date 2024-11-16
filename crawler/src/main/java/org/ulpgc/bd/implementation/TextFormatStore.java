package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.BookStore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class TextFormatStore implements BookStore {

    @Override
    public void storeBook(String urlBook, String bookId, String REPOSITORY_DOCUMENTS, Logger logger) {
        try {
            Files.createDirectories(Paths.get(REPOSITORY_DOCUMENTS));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlBook).openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(REPOSITORY_DOCUMENTS + "/" + bookId + ".txt")) {
                    in.transferTo(out);
                }
                logger.info("Book downloaded and saved at: " + REPOSITORY_DOCUMENTS + "/" + bookId + ".txt");
            }
        } catch (IOException e) {
            logger.warning("Error downloading " + urlBook + ": " + e.getMessage());
        }
    }
}
