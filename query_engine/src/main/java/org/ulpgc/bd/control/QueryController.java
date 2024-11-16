package org.ulpgc.bd.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.model.InvertedIndex;
import org.ulpgc.bd.model.Metadata;
import org.ulpgc.bd.service.interfaces.InvertedIndexService;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.ulpgc.bd.service.interfaces.MetadataService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;

public class QueryController {

    private MetadataService metadataService;
    private InvertedIndexService invertedIndexService;
    private InvertedIndexLoader invertedIndexLoader;
    private DocumentLoader documentLoader;
    private Gson gson = new Gson();

    public QueryController(
            MetadataService metadataService,
            InvertedIndexService invertedIndexService,
            InvertedIndexLoader invertedIndexLoader,
            DocumentLoader documentLoader) {
        this.metadataService = metadataService;
        this.invertedIndexService = invertedIndexService;
        this.invertedIndexLoader = invertedIndexLoader;
        this.documentLoader = documentLoader;
    }

    public Route searchMetadata = (Request req, Response res) -> {
        String title = req.queryParams("title");
        String author = req.queryParams("author");
        String year = req.queryParams("year");
        String month = req.queryParams("month");
        String day = req.queryParams("day");
        String language = req.queryParams("language");

        Map<String, String> filters = Map.of(
                "title", Optional.ofNullable(title).orElse(""),
                "author", Optional.ofNullable(author).orElse(""),
                "year", Optional.ofNullable(year).orElse(""),
                "month", Optional.ofNullable(month).orElse(""),
                "day", Optional.ofNullable(day).orElse(""),
                "language", Optional.ofNullable(language).orElse("")
        );

        List<Metadata> results = metadataService.searchMetadata(filters);

        res.type("application/json");
        return gson.toJson(results);
    };



    public Route searchInvertedIndex = (Request req, Response res) -> {
        String query = req.queryParams("query");

        if (query == null || query.isEmpty()) {
            res.status(400);
            return "No search query provided";
        }
        query = query.toLowerCase();
        Map<String, InvertedIndex> loadedIndexes = new HashMap<>();
        Map<Integer, Document> documentMap = new HashMap<>();

        for (String word : query.split(" ")) {
            InvertedIndex index = invertedIndexLoader.loadInvertedIndex(word);
            if (index != null) {
                loadedIndexes.put(word, index);
                System.out.println("Word index loaded: " + word);
            } else {
                System.err.println("No word index found: " + word);
            }
        }

        for (InvertedIndex index : loadedIndexes.values()) {
            for (InvertedIndex.DocumentData data : index.getIndex().values()) {
                for (Integer id : data.getId()) {
                    Document document = documentLoader.loadDocument(id);
                    if (document != null) {
                        documentMap.putIfAbsent(id, document);
                        System.out.println("Document loaded for ID: " + id);
                    } else {
                        System.err.println("Document not found for ID: " + id);
                    }
                }
            }
        }

        Map<String, InvertedIndex.DocumentData> results = invertedIndexService.searchInvertedIndex(query, loadedIndexes, documentMap);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(results);

        res.type("application/json");
        return jsonResponse;
    };

}
