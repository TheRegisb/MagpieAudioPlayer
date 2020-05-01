package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;

public class Configuration implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mood;

    public Configuration(String mood) {
        this.mood = mood;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}
