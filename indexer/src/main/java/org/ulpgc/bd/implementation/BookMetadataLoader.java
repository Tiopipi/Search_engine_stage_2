package org.ulpgc.bd.implementation;
import org.ulpgc.bd.control.MetadataLoader;
import org.ulpgc.bd.model.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.List;

public class BookMetadataLoader implements MetadataLoader {
    private static final Logger logger = Logger.getLogger(org.ulpgc.bd.implementation.BookMetadataLoader.class.getName());

    private Metadata extractMetadata(String text, String documentId) {
        text = text.replaceAll("\\[.*?]", "");
        Map<String, String> metadata = new HashMap<>();
        Map<String, String> metadataPatterns = new HashMap<>();
        metadataPatterns.put("title", "(Title|Título|Titre|Titel|Titolo|Título)\\s*:\\s*(.+)");
        metadataPatterns.put("author", "(Author|Autor|Auteur|Verfasser|Autore|Contributor)\\s*:\\s*(.+)");
        metadataPatterns.put("release_date", "(Release date|Fecha de publicación|Date de publication|Veröffentlichungsdatum|Data di pubblicazione|Data de publicação)\\s*:\\s*(.+)");
        metadataPatterns.put("language", "(Language|Idioma|Langue|Sprache|Lingua|Língua)\\s*:\\s*(.+)");

        String title = "", author = "", releaseDate = "", language = "";

        for (Map.Entry<String, String> entry : metadataPatterns.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String key = entry.getKey();
                String value = matcher.group(2).trim();
                switch (key) {
                    case "title":
                        title = value;
                        break;
                    case "author":
                        author = value;
                        break;
                    case "release_date":
                        releaseDate = value;
                        break;
                    case "language":
                        language = value;
                        break;
                }
            }
        }

        return new Metadata(title, author, releaseDate, language, documentId);
    }

    @Override
    public List<Metadata> loadMetadata(String directory) {
        List<Metadata> bookMetadata = new ArrayList<>();
        File folder = new File(directory);
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        Metadata metadata = extractMetadata(content.toString(), file.getName());
                        bookMetadata.add(metadata);
                    } catch (IOException e) {
                        logger.severe("Error reading file: " + file.getName() + " - " + e.getMessage());
                    }
                }
            }
        } else {
            logger.warning("Provided directory does not exist or is not a valid directory: " + directory);
        }
        return bookMetadata;
    }
}
