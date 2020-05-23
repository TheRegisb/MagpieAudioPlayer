package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable Magpie configuration bean.
 * Configuration bean containing the information about the current mood,
 * processes attributes, time slots and download batch size.
 */
public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mood;
    private List<ProcessAttributes> procAttrs = new ArrayList<>();
    private List<TimeInterval> timeIntervals = new ArrayList<>();
    private int batchSize = 2;

    /**
     * Default constructor.
     */
    public Configuration() {

    }

    /**
     * Parametric constructor.
     *
     * @param mood          Current mood literal.
     * @param procAttrs     Processes attributes.
     * @param timeIntervals Time slots attributes.
     * @param batchSize     Size of download batches.
     */
    public Configuration(String mood, List<ProcessAttributes> procAttrs, List<TimeInterval> timeIntervals, int batchSize) {
        this.mood = mood;
        this.procAttrs = procAttrs;
        this.timeIntervals = timeIntervals;
        this.batchSize = batchSize;
    }

    /**
     * Store a new process attribute.
     *
     * @param attrs Single process attributes.
     */
    public void addProcessAttributes(ProcessAttributes attrs) {
        procAttrs.add(attrs);
    }

    /**
     * Remove a process attribute.
     * @param procName Name of the process to unregister.
     * @return (1) true if a process attribute have been removed or (2) false otherwise.
     */
    public boolean removeProcessAttributesByName(String procName) {
        return procAttrs.removeIf(e -> (e.getName().equals(procName)));
    }

    /**
     * Store a new time slot.
     * @param ti Single time slot.
     */
    public void addTimeInterval(TimeInterval ti) {
        timeIntervals.add(ti);
    }

    /**
     * Remove a time slot.
     * @param tiStr Name of the time slot to unregister.
     * @return (1) true if a time slot have been removed or (2) false otherwise.
     */
    public boolean removeTimeIntervalByName(String tiStr) {
        return timeIntervals.removeIf(e -> e.equals(tiStr));
    }

    /**
     * Set current mood.
     *
     * @param mood Current mood literal.
     */
    public void setCurrentMood(String mood) {
        this.mood = mood;
    }

    /**
     * Get current mood literal.
     * @return String description of the current mood.
     */
    public String getMood() {
        return mood;
    }

    /**
     * Get all processes attributes.
     * @return The list of all processes attributes.
     */
    public List<ProcessAttributes> getProcessesAttributes() {
        return procAttrs;
    }

    /**
     * Get all time slots.
     * @return The list of all time slots.
     */
    public List<TimeInterval> getTimeIntervals() {
        return timeIntervals;
    }

    /**
     * Set download batch size.
     *
     * @param batchSize Download batch size.
     * @throws IllegalArgumentException When batchSize is lower than 1.
     */
    public void setBatchSize(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("Batch size cannot be lower than 1");
        }
        this.batchSize = batchSize;
    }

    /**
     * Get download batch size.
     * @return Download batch size.
     */
    public int getBatchSize() {
        return batchSize;
    }
}
