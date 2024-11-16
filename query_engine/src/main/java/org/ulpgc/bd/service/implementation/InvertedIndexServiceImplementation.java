package org.ulpgc.bd.service.implementation;

import org.ulpgc.bd.model.InvertedIndex;
import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.service.interfaces.InvertedIndexService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvertedIndexServiceImplementation implements InvertedIndexService {


    @Override
    public Map<String, InvertedIndex.DocumentData> searchInvertedIndex(String query, Map<String, InvertedIndex> loadedIndexes, Map<Integer, Document> documentMap) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("No search query provided");
        }

        String[] words = query.split(" ");
        Map<String, InvertedIndex.DocumentData> finalResults = new HashMap<>();
        Set<Integer> commonDocumentIds = null;
        Map<String, InvertedIndex.DocumentData> wordDataMap = new HashMap<>();

        for (String word : words) {
            InvertedIndex invertedIndex = loadedIndexes.get(word);
            if (invertedIndex != null) {
                Map<String, InvertedIndex.DocumentData> flatIndex = invertedIndex.getIndex();
                InvertedIndex.DocumentData documentData = flatIndex.get(word);
                if (documentData != null) {
                    Set<Integer> documentIds = new HashSet<>(documentData.getId());

                    if (commonDocumentIds == null) {
                        commonDocumentIds = documentIds;
                    } else {
                        commonDocumentIds.retainAll(documentIds);
                    }
                    wordDataMap.put(word, documentData);
                }
            }
        }

        if (commonDocumentIds != null && !commonDocumentIds.isEmpty()) {
            for (String word : wordDataMap.keySet()) {
                InvertedIndex.DocumentData documentData = wordDataMap.get(word);
                InvertedIndex.DocumentData filteredDocumentData = filterDocumentData(documentData, commonDocumentIds);

                finalResults.put(word, filteredDocumentData);
            }
        }

        findTitleAndParagraphtInDatalake(finalResults, documentMap);

        return finalResults;
    }


    private InvertedIndex.DocumentData filterDocumentData(InvertedIndex.DocumentData documentData, Set<Integer> commonDocumentIds) {
        List<Integer> filteredIds = new ArrayList<>();
        List<List<Integer>> filteredPositions = new ArrayList<>();
        List<Integer> filteredFrequencies = new ArrayList<>();

        for (int i = 0; i < documentData.getId().size(); i++) {
            if (commonDocumentIds.contains(documentData.getId().get(i))) {
                filteredIds.add(documentData.getId().get(i));
                filteredPositions.add(documentData.getP().get(i));
                filteredFrequencies.add(documentData.getF().get(i));
            }
        }

        InvertedIndex.DocumentData filteredDocumentData = new InvertedIndex.DocumentData();
        filteredDocumentData.setId(filteredIds);
        filteredDocumentData.setP(filteredPositions);
        filteredDocumentData.setF(filteredFrequencies);

        return filteredDocumentData;
    }


    private void findTitleAndParagraphtInDatalake(Map<String, InvertedIndex.DocumentData> queryResult, Map<Integer, Document> documentMap) {
        queryResult.forEach((word, data) -> {
            List<String> allParagraphs = new ArrayList<>();
            List<String> allTitles = new ArrayList<>();
            for (int i = 0; i < data.getId().size(); i++) {
                Integer id = data.getId().get(i);
                List<List<Integer>> positions = data.getP();
                if (positions != null && !positions.isEmpty() && documentMap.containsKey(id)) {
                    Document document = documentMap.get(id);
                    String title = extractTitle(document);
                    Integer firstPosition = positions.get(i).get(0);
                    String paragraph = extractParagraphByCharPosition(document, firstPosition);
                    allParagraphs.add(paragraph);
                    allTitles.add(title);
                    System.err.println(paragraph);
                    System.err.println(title);
                } else {
                    System.err.println("Document not found or without positions for ID: " + id);
                }
            }
            if (!allParagraphs.isEmpty()) {
                data.setParagraph(allParagraphs);
            }
            if (!allTitles.isEmpty()) {
                data.setTitle(allTitles);
            }
        });
    }



    private static String extractParagraphByCharPosition(Document document, int wordPosition) {
        String content = document.getContent();
        String regex = "\\*\\*\\* START OF THE PROJECT GUTENBERG EBOOK.*?\\*\\*\\*";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String startContent = content.substring(matcher.end()).trim();
            List<String> words = Arrays.asList(startContent.split("\\s+"));
            if (wordPosition < 0 || wordPosition >= words.size()) {
                System.err.println("Word position out of bounds");
                return "";
            }
            int start = Math.max(0, wordPosition - 10);
            int end = Math.min(words.size(), wordPosition + 11);
            return String.join(" ", words.subList(start, end));
        } else {
            System.err.println("Initial phrase not found in document");
            return "";
        }
    }

    private static String extractTitle(Document document){
        String content = document.getContent();
        String regex = "(Title|Título|Titre|Titel|Titolo|Título)\\s*:\\s*([^\\n]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String title = matcher.group(2).trim();
            return title;
        } else {
            System.err.println("Initial phrase not found in document");
            return "";
        }
    }
}
