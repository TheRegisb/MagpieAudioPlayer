package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;

public class ProcessAttributes implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Tags tags;

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

        inverse.tags.invert();
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
        return "Process name: " + name + "; " + tags.toString();
    }
}
