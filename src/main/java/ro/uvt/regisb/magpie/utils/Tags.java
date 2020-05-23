package ro.uvt.regisb.magpie.utils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Tag list and their weight.
 * Tags to be apply to a media selection. Cover "feel", "genre" and bpm.
 */
public class Tags implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Pair<String, Integer>> feel = new ArrayList<>();
    private List<Pair<String, Integer>> genre = new ArrayList<>();
    private int bpmTweak = 0;

    /**
     * Default constructor.
     */
    public Tags() {

    }

    /**
     * Copy constructor.
     *
     * @param tags Instance to copy.
     */
    public Tags(Tags tags) {
        feel = new ArrayList<>(tags.feel);
        genre = new ArrayList<>(tags.genre);
        bpmTweak = tags.bpmTweak;
    }

    /**
     * Invert tags weight.
     *
     * @return A copy of the instance's tags with their opposite weight.
     */
    public Tags invert() {
        Tags inverse = new Tags(this);

        for (Pair<String, Integer> a : inverse.genre) {
            a.setValue(a.getValue() * -1);
        }
        for (Pair<String, Integer> a : inverse.feel) {
            a.setValue(a.getValue() * -1);
        }
        inverse.bpmTweak *= -1;
        return inverse;
    }

    /**
     * Add or update a "genre" tag.
     * Add or update a "genre" tag then sort all tags on their weight first and name second.
     * @param name Name of the tag.
     * @param weight Weight of the tag. Positive for inclusion and negative for exclusion.
     */
    public void addGenre(String name, int weight) {
        for (Pair<String, Integer> entry : this.genre) {
            if (entry.getKey().equals(name)) {
                entry.setValue(entry.getValue() + weight);
                return;
            }
        }
        genre.add(MutablePair.of(name, weight));
        this.genre.sort((o1, o2) -> {
            if (Math.abs(o1.getValue()) == Math.abs(o2.getValue())) { // When value are equals
                return o1.getKey().compareTo(o2.getKey());            // sorts by name (e.g. {"action", 0) > {"calm", 0}
            }
            return (Math.abs(o1.getValue()) > Math.abs(o2.getValue()) ? -1 : 1);
        });
    }

    /**
     * Add or update a "feel" tag.
     * Add or update a "feel" tag then sort all tags on their weight first and name second.
     * @param name Name of the tag.
     * @param weight Weight of the tag. Positive for inclusion and negative for exclusion.
     */
    public void addFeel(String name, int weight) {
        for (Pair<String, Integer> entry : this.feel) {
            if (entry.getKey().equals(name)) {
                entry.setValue(entry.getValue() + weight);
                return;
            }
        }
        feel.add(MutablePair.of(name, weight));
        this.feel.sort((o1, o2) -> {
            if (Math.abs(o1.getValue()) == Math.abs(o2.getValue())) { // When value are equals
                return o1.getKey().compareTo(o2.getKey());            // sorts by name (e.g. {"action", 0) > {"calm", 0}
            }
            return (Math.abs(o1.getValue()) > Math.abs(o2.getValue()) ? -1 : 1);
        });
    }

    /**
     * Adjust BPM preference.
     * @param tweak BPM preference. Positive for high BPM and negative for low.
     */
    public void tweakBpm(int tweak) {
        bpmTweak += tweak;
    }

    /**
     * Get "genre" tags.
     * @return List of weighted "genre" tags.
     */
    public List<Pair<String, Integer>> getGenre() {
        return genre;
    }

    /**
     * Get "feel" tags.
     * @return List of weighted "feel" tags.
     */
    public List<Pair<String, Integer>> getFeel() {
        return feel;
    }

    /**
     * Get BPM tweak.
     * @return BPM tweak. Positive for high BPM, negative for low.
     */
    public int getBpmTweak() {
        return bpmTweak;
    }

    /**
     * Try to add a tags to its respective list on its name.
     * @param tagLiteral Complete tag literal.
     * @param weight Weight factor.
     * @return (1) true if the tag understood or (2) false otherwise.
     */
    public boolean parse(String tagLiteral, int weight) {
        boolean valid = true;

        if (tagLiteral.equals("Low BPM")) {
            tweakBpm(weight * -1);
        } else if (tagLiteral.equals("High BPM")) {
            tweakBpm(weight);
        } else if (tagLiteral.startsWith("Genre:")) {
            addGenre(tagLiteral.split(": ")[1], weight);
        } else if (tagLiteral.startsWith("Feel:")) {
            addFeel(tagLiteral.split(": ")[1], weight);
        } else {
            valid = false;
        }
        return valid;
    }

    /**
     * Query instance emptiness.
     * @return (1) true of no "genre", "feel" or bpm tags have been applied or (2) false otherwise.
     */
    public boolean areEmpty() {
        return genre.isEmpty() && feel.isEmpty() && bpmTweak == 0;
    }

    /**
     * Compare two tags.
     * @param tags Instance to be compared with
     * @return Equalities of the objects.
     */
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

    /**
     * Display all fields and value as human-readable string.
     * @return A descriptive string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Genre: ");
        for (Pair<String, Integer> g : genre) {
            sb.append('{').append(g.getKey()).append(", ").append(g.getValue()).append('}');
        }
        sb.append("; Feels: ");
        for (Pair<String, Integer> f : feel) {
            sb.append('{').append(f.getKey()).append(", ").append(f.getValue()).append('}');
        }
        sb.append("; BPM Tweak: ").append(bpmTweak);
        return sb.toString();
    }
}
