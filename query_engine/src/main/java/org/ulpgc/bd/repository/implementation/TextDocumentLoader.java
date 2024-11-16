package org.ulpgc.bd.repository.implementation;

import org.ulpgc.bd.model.Document;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TextDocumentLoader implements DocumentLoader {

    private String booksPath;

    public TextDocumentLoader(String booksPath) {
        this.booksPath = booksPath;
    }

    @Override
    public Document loadDocument(Integer documentId) {
        try {
            String documentPath = booksPath + "/" + documentId + ".txt";
            File file = new File(documentPath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            return new Document(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
