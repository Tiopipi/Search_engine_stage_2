package org.ulpgc.bd.apps;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import org.ulpgc.bd.control.InvertedIndexController;
import org.ulpgc.bd.control.MetadataExporter;
import org.ulpgc.bd.control.MetadataLoader;
import org.ulpgc.bd.control.BookLoader;
import org.ulpgc.bd.control.InvertedIndex;
import org.ulpgc.bd.implementation.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        String BOOKS_DIRECTORY = System.getenv("BOOKS_DIRECTORY");
        String STOP_WORDS = System.getenv("STOP_WORDS");
        String INDEX_DIRECTORY_TREE = System.getenv("INDEX_DIRECTORY_TREE");
        String INDEX_DIRECTORY_UNIQUE = System.getenv("INDEX_DIRECTORY_UNIQUE");
        String INDEX_DIRECTORY_HIERARCHICAL = System.getenv("INDEX_DIRECTORY_HIERARCHICAL");
        String METADATA_DIRECTORY = System.getenv("METADATA_DIRECTORY");
        String PROCESSED_BOOKS_PATH = System.getenv("PROCESSED_BOOKS_PATH");

        BookLoader bookLoader = new BookFileLoader();
        MetadataLoader metadataLoader = new BookMetadataLoader();
        InvertedIndex treeInvertedIndex = new TreeInvertedIndex();
        InvertedIndex uniqueJsonIndexer = new UniqueJsonInvertedIndex();
        InvertedIndex hierarchicalInvertedIndex = new HierarchicalInvertedIndex();
        MetadataExporter metadataExporter = new CsvMetadataExporter();

        InvertedIndexController controllerTree = new InvertedIndexController(bookLoader, metadataLoader, treeInvertedIndex, metadataExporter);
        InvertedIndexController controllerUnique = new InvertedIndexController(bookLoader, metadataLoader, uniqueJsonIndexer, metadataExporter);
        InvertedIndexController controllerHierarchical = new InvertedIndexController(bookLoader, metadataLoader, hierarchicalInvertedIndex, metadataExporter);


        Runnable task = () -> {
            Set<String> processedBooks = loadProcessedBooks(PROCESSED_BOOKS_PATH);



            controllerTree.processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_TREE, processedBooks);
            controllerUnique.processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_UNIQUE, processedBooks);
            controllerHierarchical.processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_HIERARCHICAL, processedBooks);

            controllerTree.processMetadata(BOOKS_DIRECTORY, METADATA_DIRECTORY, processedBooks);
            saveProcessedBooks(processedBooks);
            System.out.println("InvertedIndex and Metadata Loaded");
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(task, 0, 30, TimeUnit.MINUTES);
    }

    private static Set<String> loadProcessedBooks(String PROCESSED_BOOKS_PATH) {
        Set<String> processedBooks = new HashSet<>();

        File file = new File(PROCESSED_BOOKS_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processedBooks.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return processedBooks;
    }

    private static void saveProcessedBooks(Set<String> processedBooks) {
        String PROCESSED_BOOKS_PATH = System.getenv("PROCESSED_BOOKS_PATH");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PROCESSED_BOOKS_PATH))) {
            for (String docId : processedBooks) {
                writer.write(docId);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
