package ro.uvt.regisb.magpie.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Pair<String, Integer>> attributes = new ArrayList<>();
    private int bpmTweak = 0;

    public ProcessAttributes(String name) {
        this.name = name;
    }

    public void addAttributes(String name, int weight) {
        for (Pair<String, Integer> entry : this.attributes) {
            if (entry.getKey().equals(name)) {
                entry.setValue(entry.getValue() + weight);
                return;
            }
        }
        attributes.add(Pair.of(name, weight));
    }

    public void setBpmTweak(int bpmWeight) {
        bpmTweak = bpmWeight;
    }

    public String getName() {
        return name;
    }

    public List<Pair<String, Integer>> getAttributes() {
        return attributes;
    }

    public int getBpmTweak() {
        return bpmTweak;
    }
}
