package ro.uvt.regisb.magpie.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WeightedMediaFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Pair<String, Integer>> genre;
    private List<Pair<String, Integer>> feel;
    private int lowBPMCount;
    private int highBPMCount;

    // Default constructor
    public WeightedMediaFilter() {
        genre = new ArrayList<>();
        feel = new ArrayList<>();
    }

    // Functions
    public void addGenre(String genre, int count) {
        // Checking if genre not already in list
        for (Pair<String, Integer> entry : this.genre) {
            if (entry.getKey().equals(genre)) {
                entry.setValue(entry.getValue() + count);
                return;
            }
        }
        // Function did not returned, inserting pair.
        this.genre.add(Pair.of(genre, count));
        // TODO sort genre by count
    }

    public void addGenre(String genre) {
        addGenre(genre, 1);
    }

    public void addFeel(String feel, int count) {
        // Checking if feel not already in list
        for (Pair<String, Integer> entry : this.feel) {
            if (entry.getKey().equals(feel)) {
                entry.setValue(entry.getValue() + 1);
                return;
            }
        }
        // Function did not returned, inserting pair
        this.feel.add(Pair.of(feel, count));
    }

    public void addFeel(String feel) {
        addFeel(feel, 1);
    }

    public void tweakLowBPMCount(int count) {
        lowBPMCount += count;
    }

    public void tweakHighBPMCount(int count) {
        highBPMCount += count;
    }

    // Procedural getters
    public boolean isLowBPM() {
        return lowBPMCount > 1; // TODO improve formula
    }

    public boolean isHighBPM() {
        return highBPMCount > 1; // TODO improve formula
    }

    // Generic getters
    public List<Pair<String, Integer>> getGenre() {
        return genre;
    }

    public List<Pair<String, Integer>> getFeel() {
        return feel;
    }
}
