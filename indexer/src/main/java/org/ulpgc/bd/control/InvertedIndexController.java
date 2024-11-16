package org.ulpgc.bd.control;

import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndexEntry;
import org.ulpgc.bd.model.Metadata;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class InvertedIndexController {

    private static final Logger logger = Logger.getLogger(InvertedIndexController.class.getName());
    private final BookLoader bookLoader;
    private final MetadataLoader metadataLoader;
    private final InvertedIndex invertedIndex;
    private final MetadataExporter metadataExporter;

    public InvertedIndexController(BookLoader bookLoader, MetadataLoader metadataLoader, InvertedIndex invertedIndex, MetadataExporter metadataExporter) {
        this.bookLoader = bookLoader;
        this.metadataLoader = metadataLoader;
        this.invertedIndex = invertedIndex;
        this.metadataExporter = metadataExporter;
    }

    public void processInvertedIndex(String booksDirectory, String stopWordsFilePath, String indexDirectory, Set<String> processedBooks) {
        try {
            List<Document> documents = bookLoader.loadBooksFromDirectory(booksDirectory, processedBooks);
            bookLoader.initializeStopWords(stopWordsFilePath);
            if (processedBooks.isEmpty()) {
                Map<String, InvertedIndexEntry> invertedIndexMap = invertedIndex.buildInvertedIndexWithPositions(documents, bookLoader.getStopWords(), processedBooks, indexDirectory);
                invertedIndex.exportToJson(invertedIndexMap, indexDirectory);
            } else{
                Map<String, InvertedIndexEntry> newEntries = invertedIndex.buildInvertedIndexWithPositions(documents, bookLoader.getStopWords(), processedBooks, indexDirectory);
                invertedIndex.updateInvertedIndexJson(newEntries, indexDirectory);
            }



        } catch (Exception e) {
            logger.severe("Error during InvertedIndex processing: " + e.getMessage());
        }
    }

    public void processMetadata(String booksDirectory, String metadataDirectory, Set<String> processedBooks) {
        try {
            List<Metadata> metadata = metadataLoader.loadMetadata(booksDirectory);
            metadataExporter.exportMetadataToCsv(metadata, metadataDirectory, processedBooks);
        } catch (Exception e) {
            logger.severe("Error during metadata processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
