package org.ulpgc.bd.repository.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ulpgc.bd.model.InvertedIndex;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HierarchicalInvertedIndexLoader implements InvertedIndexLoader {

    private String invertedIndexPath;

    public HierarchicalInvertedIndexLoader(String invertedIndexPath) {
        this.invertedIndexPath = invertedIndexPath;
    }
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public InvertedIndex loadInvertedIndex(String searchKey) {
        InvertedIndex index = new InvertedIndex();
        Map<String, InvertedIndex.DocumentData> indexMap = new HashMap<>();

        try {
            Path basePath = Paths.get(invertedIndexPath);
            Path currentPath = basePath;
            if (searchKey.length() == 1){
                currentPath = currentPath.resolve(searchKey.substring(0,1));
                currentPath = currentPath.resolve(searchKey.substring(0,1));
                currentPath = currentPath.resolve(searchKey.substring(0,1));

            } else if (searchKey.length() == 2) {
                currentPath = currentPath.resolve(searchKey.substring(0,1));
                currentPath = currentPath.resolve(searchKey.substring(0,2));
                currentPath = currentPath.resolve(searchKey.substring(0,2));
            }else{
                currentPath = currentPath.resolve(searchKey.substring(0, 1));
                currentPath = currentPath.resolve(searchKey.substring(0, 2));
                currentPath = currentPath.resolve(searchKey.substring(0, 3));
            }

            Path targetFile = currentPath.resolve(searchKey + ".json");
            if (Files.exists(targetFile)) {
                String content = new String(Files.readAllBytes(targetFile));
                Map<String, InvertedIndex.DocumentData> partialIndex = mapper.readValue(
                        content,
                        new TypeReference<Map<String, InvertedIndex.DocumentData>>() {}
                );
                indexMap.putAll(partialIndex);
            }
            index.setIndex(indexMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return index;
    }
}
