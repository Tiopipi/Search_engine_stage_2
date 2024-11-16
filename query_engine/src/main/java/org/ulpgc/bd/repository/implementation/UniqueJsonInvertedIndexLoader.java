package org.ulpgc.bd.repository.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.ulpgc.bd.model.InvertedIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UniqueJsonInvertedIndexLoader implements InvertedIndexLoader {

    private String invertedIndexPath;

    public UniqueJsonInvertedIndexLoader(String invertedIndexPath) {
        this.invertedIndexPath = invertedIndexPath;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public InvertedIndex loadInvertedIndex(String query) {
        List<String> words = Arrays.asList(query.split(" "));
        Map<String, InvertedIndex.DocumentData> index = new HashMap<>();

        for (String word : words) {
            word = word.trim().toLowerCase();

            String jsonFilePath = invertedIndexPath;

            try {
                System.out.println("Cargando archivo JSON: " + jsonFilePath);
                String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
                content = content.replaceAll("(?<=\\d)\\.(?=\\s|,|})", ".0");

                Map<String, InvertedIndex.DocumentData> letterIndex = objectMapper.readValue(content, new TypeReference<>() {});
                if (letterIndex.containsKey(word)) {
                    index.put(word, letterIndex.get(word));
                }
            } catch (IOException e) {
                System.err.println("Error al cargar el archivo JSON para la palabra: " + word);
                e.printStackTrace();
            }
        }

        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.setIndex(index);
        return invertedIndex;
    }

}
