package org.ulpgc.bd.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class InvertedIndexEntry {
    @SerializedName("id")
    private final List<String> docIds = new ArrayList<>();

    @SerializedName("p")
    private final List<List<Integer>> positions = new ArrayList<>();

    @SerializedName("f")
    private final List<Integer> frequencies = new ArrayList<>();

    public void addPosition(String docId, int position) {
        String processedDocId = docId.replace(".txt", "");

        int index = docIds.indexOf(processedDocId);
        if (index == -1) {
            docIds.add(processedDocId);
            positions.add(new ArrayList<>(List.of(position)));
            frequencies.add(1);
        } else {
            positions.get(index).add(position);
            frequencies.set(index, frequencies.get(index) + 1);
        }
    }


    public void mergeWith(InvertedIndexEntry newEntry) {
        for (int i = 0; i < newEntry.getDocIds().size(); i++) {
            String newDocId = newEntry.getDocIds().get(i);
            int index = docIds.indexOf(newDocId);

            if (index != -1) {
                positions.get(index).addAll(newEntry.getPositions().get(i));
                frequencies.set(index, frequencies.get(index) + newEntry.getFrequencies().get(i));
            } else {
                docIds.add(newDocId);
                positions.add(newEntry.getPositions().get(i));
                frequencies.add(newEntry.getFrequencies().get(i));
            }
        }
    }

    public List<String> getDocIds() {
        return docIds;
    }

    public List<List<Integer>> getPositions() {
        return positions;
    }

    public List<Integer> getFrequencies() {
        return frequencies;
    }
}
