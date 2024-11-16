package org.ulpgc.bd.repository.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.ulpgc.bd.model.InvertedIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeDataStructureInvertedIndexLoader implements InvertedIndexLoader {

    private String invertedIndexPath;

    public TreeDataStructureInvertedIndexLoader(String invertedIndexPath) {
        this.invertedIndexPath = invertedIndexPath;
    }

    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public InvertedIndex loadInvertedIndex(String query) {
        List<String> words = Arrays.asList(query.toLowerCase().split(" "));
        Map<String, InvertedIndex.DocumentData> index = new HashMap<>();
        for (String word : words) {
            String jsonFilePath = String.format("%s/%s/%s_words.json", invertedIndexPath, word.charAt(0), word.charAt(0));
            try {
                System.out.println("Loading JSON file: " + jsonFilePath);
                String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                content = content.replaceAll("(?<=\\d)\\.(?=\\s|,|})", ".0");
                Map<String, InvertedIndex.DocumentData> letterIndex = mapper.readValue(content, new TypeReference<>() {});
                if (letterIndex.containsKey(word)) {
                    index.put(word, letterIndex.get(word));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.setIndex(index);
        return invertedIndex;
    }
}
