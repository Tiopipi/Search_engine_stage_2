package org.ulpgc.bd.repository.implementation;

import org.ulpgc.bd.repository.interfaces.MetadataLoader;
import org.ulpgc.bd.model.Metadata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class CsvMetadataLoader implements MetadataLoader {

    @Override
    public List<Metadata> loadMetadata(String METADATA_FILE_PATH) {
        List<Metadata> metadataList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(METADATA_FILE_PATH))) {
            String[] headers = reader.readLine().split(";");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                Metadata metadata = new Metadata(
                        values[Arrays.asList(headers).indexOf("title")],
                        values[Arrays.asList(headers).indexOf("author")],
                        convertDate(values[Arrays.asList(headers).indexOf("release_date")]),
                        values[Arrays.asList(headers).indexOf("language")],
                        values[Arrays.asList(headers).indexOf("document")]
                );
                metadataList.add(metadata);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return metadataList;
    }

    private String convertDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            dateStr = dateStr.trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(Locale.ENGLISH);
            LocalDate date = LocalDate.parse(dateStr, formatter);
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            System.err.println("Error while transforming the date: '" + dateStr + "'");
            return "";
        }
    }
}
