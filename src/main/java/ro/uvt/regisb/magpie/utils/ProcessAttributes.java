package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;

public class ProcessAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Tags tags;
    private transient boolean isActive = false;

    public ProcessAttributes(String name) {
        this.name = name;
        this.tags = new Tags();
    }

    private ProcessAttributes(ProcessAttributes attrs) {
        this.name = attrs.name;
        this.tags = new Tags(attrs.tags);
    }

    public ProcessAttributes invert() {
        ProcessAttributes inverse = new ProcessAttributes(this);

        inverse.tags = this.tags.invert();
        return inverse;
    }

    public Tags getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "Process name: " + name + "; Active: " + isActive + "; Tags: " + tags.toString();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
