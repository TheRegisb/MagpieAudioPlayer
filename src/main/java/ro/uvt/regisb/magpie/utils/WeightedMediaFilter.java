package ro.uvt.regisb.magpie.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WeightedMediaFilter implements Serializable {
    public enum BPMClassification {
        ENERGETIC,
        NOT_CALM,
        NEUTRAL,
        NOT_ENERGETIC,
        CALM
    }

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
        boolean alreadyPresent = false;

        for (Pair<String, Integer> entry : this.genre) {
            if (entry.getKey().equals(genre)) {
                entry.setValue(entry.getValue() + count);
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent) {
            this.genre.add(Pair.of(genre, count));
        }
        this.genre.sort((o1, o2) -> {
            if (Math.abs(o1.getValue()) == Math.abs(o2.getValue())) {
                return 0;
            }
            return (Math.abs(o1.getValue()) > Math.abs(o2.getValue()) ? -1 : 1);
        });
    }

    public void addGenre(String genre) {
        addGenre(genre, 1);
    }

    public void addFeel(String feel, int count) {
        boolean alreadyPresent = false;
        // Checking if feel not already in list
        for (Pair<String, Integer> entry : this.feel) {
            if (entry.getKey().equals(feel)) {
                entry.setValue(entry.getValue() + count);
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent) {
            this.feel.add(Pair.of(feel, count));
        }
        this.feel.sort((o1, o2) -> {
            if (Math.abs(o1.getValue()) == Math.abs(o2.getValue())) {
                return 0;
            }
            return (Math.abs(o1.getValue()) > Math.abs(o2.getValue()) ? -1 : 1);
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
    public BPMClassification getBPM() {
        if (highBPMCount == 0 && lowBPMCount == 0) {
            return BPMClassification.NEUTRAL;
        }

        double ratio = Math.abs((double) highBPMCount - lowBPMCount) / Math.max(Math.abs(highBPMCount), Math.abs(lowBPMCount));
        boolean highIsMajor;

        if (Math.abs(highBPMCount) > Math.abs(lowBPMCount)) {
            highIsMajor = true;
        } else {
            highIsMajor = highBPMCount > lowBPMCount;
        }
        if (ratio >= 0 && ratio <= 0.2) {
            return BPMClassification.NEUTRAL;
        } else if (ratio > 0.2 && ratio <= 0.6) {
            return (highIsMajor ? BPMClassification.NOT_CALM : BPMClassification.NOT_ENERGETIC);
        } else {
            return (highIsMajor ? BPMClassification.ENERGETIC : BPMClassification.CALM);
        }
    }

    // Generic getters
    public List<Pair<String, Integer>> getGenre() {
        return genre;
    }

    public List<Pair<String, Integer>> getFeel() {
        return feel;
    }
}
