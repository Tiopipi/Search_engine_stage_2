package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.MetadataExporter;
import org.ulpgc.bd.model.Metadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CsvMetadataExporter implements MetadataExporter {

    @Override
    public void exportMetadataToCsv(List<Metadata> metadatalist, String metadataDirectory, Set<String> processedBooks) {
        Path parentDir = Paths.get(metadataDirectory);
        Path outputPath = parentDir.resolve("metadata.csv");
        String outputFile = outputPath.toString();

        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                System.err.println("Error creating directories: " + e.getMessage());
                return;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            boolean fileExists = Files.exists(outputPath);
            if (!fileExists || Files.size(outputPath) == 0) {
                if (!metadatalist.isEmpty()) {
                    writer.write("release_date;author;document;language;title");
                    writer.newLine();
                }
            }
            for (Metadata metadata : metadatalist) {
                String bookFileName = metadata.getId();
                if (!processedBooks.contains(bookFileName)) {
                    List<String> row = Arrays.asList(
                            metadata.getReleaseDate(),
                            metadata.getAuthor(),
                            bookFileName,
                            metadata.getLanguage(),
                            metadata.getTitle()
                    );
                    writer.write(String.join(";", row));
                    writer.newLine();
                    processedBooks.add(bookFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
