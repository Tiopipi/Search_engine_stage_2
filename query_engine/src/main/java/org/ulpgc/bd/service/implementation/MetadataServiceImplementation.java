package org.ulpgc.bd.service.implementation;

import org.ulpgc.bd.model.Metadata;
import org.ulpgc.bd.service.interfaces.MetadataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetadataServiceImplementation implements MetadataService {

    private List<Metadata> metadataList;

    public MetadataServiceImplementation(List<Metadata> metadataList) {
        this.metadataList = metadataList;
    }

    @Override
    public List<Metadata> searchMetadata(Map<String, String> filters) {
        List<Metadata> results = new ArrayList<>();

        for (Metadata metadata : metadataList) {
            boolean matches = filters.entrySet().stream().allMatch(filter -> {
                String filterKey = filter.getKey();
                String filterValue = filter.getValue();

                if (filterValue == null || filterValue.trim().isEmpty()) {
                    return true;
                }

                String metadataValue = switch (filterKey) {
                    case "title" -> metadata.getTitle() != null ? metadata.getTitle().toLowerCase() : "";
                    case "author" -> metadata.getAuthor() != null ? metadata.getAuthor().toLowerCase() : "";
                    case "language" -> metadata.getLanguage() != null ? metadata.getLanguage().toLowerCase() : "";
                    case "year" -> extractYear(metadata.getReleaseDate());
                    case "month" -> extractMonth(metadata.getReleaseDate());
                    case "day" -> extractDay(metadata.getReleaseDate());
                    default -> "";
                };

                return metadataValue.contains(filterValue.toLowerCase());
            });

            if (matches) {
                results.add(metadata);
            }
        }
        return results;
    }

    private String extractYear(String releaseDate) {
        if (releaseDate != null && !releaseDate.isEmpty()) {
            return releaseDate.split("-")[0];
        }
        return "";
    }

    private String extractMonth(String releaseDate) {
        if (releaseDate != null && releaseDate.split("-").length > 1) {
            return releaseDate.split("-")[1];
        }
        return "";
    }

    private String extractDay(String releaseDate) {
        if (releaseDate != null && releaseDate.split("-").length > 2) {
            return releaseDate.split("-")[2];
        }
        return "";
    }
}
