package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.ulpgc.bd.control.BookLoader;
import org.ulpgc.bd.control.InvertedIndex;
import org.ulpgc.bd.implementation.TreeInvertedIndex;
import org.ulpgc.bd.implementation.UniqueJsonInvertedIndex;
import org.ulpgc.bd.implementation.HierarchicalInvertedIndex;
import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.implementation.BookFileLoader;
import org.ulpgc.bd.model.InvertedIndexEntry;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class InvertedIndexTest {

    private final String BOOKS_DIRECTORY = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_2/Datalake/eventstore/gutenbrg";
    private final String STOP_WORDS = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_2/Stop_words.txt";
    private final String INDEX_DIRECTORY_TREE = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_2/Datamarts/Inverted Index/Tree Data Structure";
    private final String INDEX_DIRECTORY_UNIQUE = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_2/Datamarts/Inverted Index/Unique Data Structure";
    private final String INDEX_DIRECTORY_HIERARCHICAL = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_2/Datamarts/Inverted Index/Hierarchical Data Structure";
    private final String PROCESSED_BOOKS_FILE = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_2/Processed_books.txt ";

    private List<Document> documents;
    private InvertedIndex uniqueInvertedIndex;
    private InvertedIndex treeInvertedIndex;
    private InvertedIndex hierarchicalInvertedIndex;
    private Set<String> processedBooks;
    private Set<String> stopWords;

    @Setup(Level.Trial)
    public void setup() throws IOException {
        BookLoader bookLoader = new BookFileLoader();
        bookLoader.initializeStopWords(STOP_WORDS);
        stopWords = bookLoader.getStopWords();

        processedBooks = loadProcessedBooks(PROCESSED_BOOKS_FILE);
        documents = bookLoader.loadBooksFromDirectory(BOOKS_DIRECTORY, processedBooks);

        uniqueInvertedIndex = new UniqueJsonInvertedIndex();
        treeInvertedIndex = new TreeInvertedIndex();
        hierarchicalInvertedIndex = new HierarchicalInvertedIndex();

        new File(INDEX_DIRECTORY_TREE).mkdirs();
        new File(INDEX_DIRECTORY_UNIQUE).mkdirs();
        new File(INDEX_DIRECTORY_HIERARCHICAL).mkdirs();
    }

    @Benchmark
    public void benchmarkUniqueJsonInvertedIndexExport() {
        exportUniqueJsonInvertedIndex();
    }

    @Benchmark
    public void benchmarkTreeInvertedIndexExport() {
        exportTreeInvertedIndex();
    }

    @Benchmark
    public void benchmarkHierarchicalInvertedIndexExport() {
        exportHierarchicalInvertedIndex();
    }

    private void exportUniqueJsonInvertedIndex() {
        Map<String, InvertedIndexEntry> index = uniqueInvertedIndex.buildInvertedIndexWithPositions(documents, stopWords, processedBooks, INDEX_DIRECTORY_UNIQUE);
        uniqueInvertedIndex.exportToJson(index, INDEX_DIRECTORY_UNIQUE);
    }

    private void exportTreeInvertedIndex() {
        Map<String, InvertedIndexEntry> index = treeInvertedIndex.buildInvertedIndexWithPositions(documents, stopWords, processedBooks, INDEX_DIRECTORY_TREE);
        treeInvertedIndex.exportToJson(index, INDEX_DIRECTORY_TREE);
    }

    private void exportHierarchicalInvertedIndex() {
        Map<String, InvertedIndexEntry> index = hierarchicalInvertedIndex.buildInvertedIndexWithPositions(documents, stopWords, processedBooks, INDEX_DIRECTORY_HIERARCHICAL);
        hierarchicalInvertedIndex.exportToJson(index, INDEX_DIRECTORY_HIERARCHICAL);
    }

    private Set<String> loadProcessedBooks(String processedBooksFile) {
        Set<String> processedBooks = new HashSet<>();
        File file = new File(processedBooksFile);
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
}
