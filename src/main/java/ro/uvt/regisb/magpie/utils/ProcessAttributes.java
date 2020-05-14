package ro.uvt.regisb.magpie.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Pair<String, Integer>> feel = new ArrayList<>();
    private List<Pair<String, Integer>> genre = new ArrayList<>();
    private int bpmTweak = 0;

    public ProcessAttributes(String name) {
        this.name = name;
    }

    private ProcessAttributes(ProcessAttributes attrs) {
        this.name = attrs.name;
        this.feel = new ArrayList<>();
        this.genre = new ArrayList<>();
        this.bpmTweak = attrs.bpmTweak;
    }

    public ProcessAttributes invert() {
        ProcessAttributes inverse = new ProcessAttributes(this);

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
    }

    public void addFeel(String name, int weight) {
        for (Pair<String, Integer> entry : this.feel) {
            if (entry.getKey().equals(name)) {
                entry.setValue(entry.getValue() + weight);
                return;
            }
        }
        feel.add(Pair.of(name, weight));
    }

    public void tweakBpm(int tweak) {
        bpmTweak += tweak;
    }

    public void setBpmTweak(int bpmWeight) {
        bpmTweak = bpmWeight;
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Process Name: ").append(name).append("; Genres: ");
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
