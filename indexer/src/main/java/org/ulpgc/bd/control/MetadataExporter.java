package org.ulpgc.bd.control;

import org.ulpgc.bd.model.Metadata;

import java.util.List;
import java.util.Set;

public interface MetadataExporter {
    void exportMetadataToCsv(List<Metadata> metadatalist, String outputFile, Set<String> processedBooks);
}
