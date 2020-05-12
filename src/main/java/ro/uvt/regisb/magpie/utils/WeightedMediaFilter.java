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

    public WeightedMediaFilter(WeightedMediaFilter filter) {
        this.genre = new ArrayList<>(filter.genre);
        this.feel = new ArrayList<>(filter.feel);
        this.lowBPMCount = filter.lowBPMCount;
        this.highBPMCount = filter.highBPMCount;
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
        this.genre.sort((o1, o2) -> {
            if (o1.getValue().equals(o2.getValue())) {
                return 0;
            }
            return (o1.getValue() > o2.getValue() ? -1 : 1);
        });
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
        this.feel.sort((o1, o2) -> {
            if (o1.getValue().equals(o2.getValue())) {
                return 0;
            }
            return (o1.getValue() > o2.getValue() ? -1 : 1);
        });
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

    public WeightedMediaFilter loosenConstrains() {
        WeightedMediaFilter filter = new WeightedMediaFilter(this);

        // If no list is empty, then remove the lowest ranking tag among them
        if (!(filter.getGenre().isEmpty() || filter.getFeel().isEmpty())) {
            if (filter.getGenre().get(filter.getGenre().size() - 1).getValue()
                    > filter.getFeel().get(filter.getFeel().size() - 1).getValue()) {
                filter.getFeel().remove(filter.getFeel().size() - 1);
            } else {
                filter.getGenre().remove(filter.getGenre().size() - 1);
            }
        } else if (!filter.getFeel().isEmpty()) { // Otherwise, if feel isn't empty, remove the lowest ranking tag
            filter.getFeel().remove(filter.getFeel().size() - 1);
        } else if (!filter.getGenre().isEmpty()) { // Otherwise, if genre isn't empty, remove the lowest ranking tag
            filter.getGenre().remove(filter.getGenre().size() - 1);
        } // Otherwise, both lists are empty and no loosening operation can be performed.
        return filter;
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
