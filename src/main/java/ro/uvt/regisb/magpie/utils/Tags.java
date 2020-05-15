package ro.uvt.regisb.magpie.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tags implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Pair<String, Integer>> feel = new ArrayList<>();
    private List<Pair<String, Integer>> genre = new ArrayList<>();
    private int bpmTweak = 0;

    public Tags() {

    }

    public Tags(Tags tags) {
        bpmTweak = tags.bpmTweak;
    }

    public Tags invert() {
        Tags inverse = new Tags(this);

        for (Pair<String, Integer> a : this.genre) {
            inverse.addGenre(a.getKey(), a.getValue() * -1);
        }
        for (Pair<String, Integer> a : this.feel) {
            inverse.addFeel(a.getKey(), a.getValue() * -1);
        }
        inverse.bpmTweak *= -1;
        return inverse;
    }

    public void addGenre(String name, int weight) {
        for (Pair<String, Integer> entry : this.genre) {
            if (entry.getKey().equals(name)) {
                entry.setValue(entry.getValue() + weight);
                return;
            }
        }
        genre.add(Pair.of(name, weight));
        this.genre.sort((o1, o2) -> {
            if (Math.abs(o1.getValue()) == Math.abs(o2.getValue())) { // When value are equals
                return o1.getKey().compareTo(o2.getKey());            // sorts by name (e.g. {"action", 0) > {"calm", 0}
            }
            return (Math.abs(o1.getValue()) > Math.abs(o2.getValue()) ? -1 : 1);
        });
    }

    public void addFeel(String name, int weight) {
        for (Pair<String, Integer> entry : this.feel) {
            if (entry.getKey().equals(name)) {
                entry.setValue(entry.getValue() + weight);
                return;
            }
        }
        feel.add(Pair.of(name, weight));
        this.feel.sort((o1, o2) -> {
            if (Math.abs(o1.getValue()) == Math.abs(o2.getValue())) { // When value are equals
                return o1.getKey().compareTo(o2.getKey());            // sorts by name (e.g. {"action", 0) > {"calm", 0}
            }
            return (Math.abs(o1.getValue()) > Math.abs(o2.getValue()) ? -1 : 1);
        });
    }

    public void tweakBpm(int tweak) {
        bpmTweak += tweak;
    }

    public void setBpmTweak(int bpmWeight) {
        bpmTweak = bpmWeight;
    }

    public List<Pair<String, Integer>> getGenre() {
        return genre;
    }

    public List<Pair<String, Integer>> getFeel() {
        return feel;
    }

    public int getBpmTweak() {
        return bpmTweak;
    }

    public boolean parse(String tagLiteral) {
        boolean valid = true;

        if (tagLiteral.equals("Low BPM")) {
            tweakBpm(-1);
        } else if (tagLiteral.equals("High BPM")) {
            tweakBpm(1);
        } else if (tagLiteral.startsWith("Genre:")) {
            addGenre(tagLiteral.split(": ")[1], 1);
        } else if (tagLiteral.startsWith("Feel:")) {
            addFeel(tagLiteral.split(": ")[1], 1);
        } else {
            valid = false;
        }
        return valid;
    }

    public boolean areEmpty() {
        return genre.isEmpty() && feel.isEmpty() && bpmTweak == 0;
    }

    public boolean equals(Tags tags) {
        if (this.genre.size() != tags.genre.size() // Check for equality in number of tags
                || this.feel.size() != tags.feel.size()) {
            return false;
        }
        for (int i = 0; i != this.genre.size(); i++) {
            if (this.genre.get(i).compareTo(tags.genre.get(i)) != 0) {
                return false;
            }
        }
        for (int i = 0; i != this.feel.size(); i++) {
            if (this.feel.get(i).compareTo(tags.genre.get(i)) != 0) {
                return false;
            }
        }
        return this.bpmTweak == tags.bpmTweak;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Pair<String, Integer> g : genre) {
            sb.append('<').append(g.getKey()).append(", ").append(g.getValue()).append('>');
        }
        sb.append("; Feels: ");
        for (Pair<String, Integer> f : feel) {
            sb.append('<').append(f.getKey()).append(", ").append(f.getValue()).append('>');
        }
        sb.append("; BPM Tweak: ").append(bpmTweak);
        return sb.toString();
    }
}