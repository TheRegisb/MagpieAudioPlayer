package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mood;
    private List<ProcessAttributes> procAttrs = new ArrayList<>();
    private List<TimeInterval> timeIntervals = new ArrayList<>();

    public Configuration() {

    }

    public Configuration(String mood, List<ProcessAttributes> procAttrs, List<TimeInterval> timeIntervals) {
        this.mood = mood;
        this.procAttrs = procAttrs;
        this.timeIntervals = timeIntervals;
    }

    public void addProcessAttributes(ProcessAttributes attrs) {
        procAttrs.add(attrs);
    }

    public boolean removeProcessAttributesByName(String procName) {
        return procAttrs.removeIf(e -> (e.getName().equals(procName)));
    }

    public void addTimeInterval(TimeInterval ti) {
        timeIntervals.add(ti);
    }

    public boolean removeTimeIntervalByName(String tiStr) {
        return timeIntervals.removeIf(e -> e.equals(tiStr));
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public List<ProcessAttributes> getProcessesAttributes() {
        return procAttrs;
    }

    public List<TimeInterval> getTimeIntervals() {
        return timeIntervals;
    }
}
