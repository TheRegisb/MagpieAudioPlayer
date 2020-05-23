package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;

/**
 * Monitored process weighed tags.
 * Encompass a named system process and its weighed tags.
 *
 * @see Tags
 */
public class ProcessAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Tags tags;
    private transient boolean isActive = false;

    /**
     * Default constructor.
     * Create a named process attributes with empty tags.
     *
     * @param name Name of the system process to be bound to.
     */
    public ProcessAttributes(String name) {
        this.name = name;
        this.tags = new Tags();
    }

    /**
     * Copy constructor.
     * @param attrs Instance to copy.
     */
    private ProcessAttributes(ProcessAttributes attrs) {
        this.name = attrs.name;
        this.tags = new Tags(attrs.tags);
    }

    /**
     * Inverse the tags weight of the process.
     * @return A copy of the instance with the opposite tags weight.
     */
    public ProcessAttributes invert() {
        ProcessAttributes inverse = new ProcessAttributes(this);

        inverse.tags = this.tags.invert();
        return inverse;
    }

    /**
     * Get tags.
     * @return Process' tags.
     * @see Tags
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Get process name.
     * @return Process name.
     */
    public String getName() {
        return name;
    }


    /**
     * Describe the instance in a human-readable fashion.
     * @return A string describing the value of each field of the instance.
     */
    @Override
    public String toString() {
        return "Process name: " + name + "; Active: " + isActive + "; Tags: " + tags.toString();
    }

    /**
     * Get process activity.
     * @return (1) true when the attributes are in effect or (2) false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Set process activity.
     * Enable or disable the attributes effects.
     * @param active Bound process activity.
     */
    public void setActive(boolean active) {
        isActive = active;
    }
}
