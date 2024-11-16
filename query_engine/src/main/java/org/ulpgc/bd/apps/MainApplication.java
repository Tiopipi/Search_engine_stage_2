package org.ulpgc.bd.apps;

import static spark.Spark.*;
import org.ulpgc.bd.control.QueryController;
import org.ulpgc.bd.model.Metadata;
import org.ulpgc.bd.repository.implementation.*;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.ulpgc.bd.service.implementation.*;
import org.ulpgc.bd.service.interfaces.InvertedIndexService;
import org.ulpgc.bd.service.interfaces.MetadataService;
import spark.Spark;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MainApplication {
    public static void main(String[] args) {
        Spark.port(8080);
        Spark.staticFiles.location("public");

        String METADATA_FILE_PATH = System.getenv("METADATA_FILE_PATH");
        String UNIQUE_FILE_PATH = System.getenv("UNIQUE_FILE_PATH");
        String TREE_DIRECTORY = System.getenv("TREE_DIRECTORY");
        String HIERARCHICAL_DIRECTORY = System.getenv("HIERARCHICAL_DIRECTORY");
        String BOOKS_FILE_PATH = System.getenv("BOOKS_FILE_PATH");

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });

        List<Metadata> metadataList = new CsvMetadataLoader().loadMetadata(METADATA_FILE_PATH);
        DocumentLoader documentLoader = new TextDocumentLoader(BOOKS_FILE_PATH);

        InvertedIndexLoader treeInvertedIndexLoader = new TreeDataStructureInvertedIndexLoader(TREE_DIRECTORY);
        InvertedIndexLoader uniqueInvertedIndexLoader = new UniqueJsonInvertedIndexLoader(UNIQUE_FILE_PATH);
        InvertedIndexLoader hierarchicalInvertedIndexLoader = new HierarchicalInvertedIndexLoader(HIERARCHICAL_DIRECTORY);

        MetadataService metadataService = new MetadataServiceImplementation(metadataList);
        InvertedIndexService invertedIndexService = new InvertedIndexServiceImplementation();

        QueryController treeQueryController = new QueryController(metadataService, invertedIndexService, treeInvertedIndexLoader, documentLoader);
        QueryController uniqueQueryController = new QueryController(metadataService, invertedIndexService, uniqueInvertedIndexLoader, documentLoader);
        QueryController hierarchicalQueryController = new QueryController(metadataService, invertedIndexService, hierarchicalInvertedIndexLoader, documentLoader);


        Spark.get("/search/metadata", hierarchicalQueryController.searchMetadata);
        Spark.get("/search/hierarchical", hierarchicalQueryController.searchInvertedIndex);
        Spark.get("/search/tree", treeQueryController.searchInvertedIndex);
        Spark.get("/search/unique", uniqueQueryController.searchInvertedIndex);
        Spark.get("/books/:filename", (req, res) -> {
            String filename = req.params(":filename");
            Path filePath = Paths.get(BOOKS_FILE_PATH, filename);

            if (Files.exists(filePath)) {
                String contentType = Files.probeContentType(filePath);
                res.type(contentType != null ? contentType : "application/octet-stream");

                return Files.readAllBytes(filePath);
            } else {
                res.status(404);
                return "File not found";
            }
        });

        openHtmlInBrowser("http://localhost:8080/gui.html");

        System.out.println("Server executing on port 8080...");
    }

    private static void openHtmlInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                URI uri = new URI(url);
                desktop.browse(uri);
            } else {
                System.out.println("Desktop is not supported on this platform.");
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error opening the browser: " + e.getMessage());
        }
    }
}
