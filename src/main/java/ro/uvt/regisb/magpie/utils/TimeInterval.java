package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeInterval implements Serializable {
    private Calendar startTime = Calendar.getInstance();
    private Calendar stopTime = Calendar.getInstance();
    private Tags tags = new Tags();
    private transient boolean isActive = false;

    public TimeInterval(String start, String stop) throws ParseException {
        startTime.setTime(new SimpleDateFormat("HH:mm").parse(start));
        stopTime.setTime(new SimpleDateFormat("HH:mm").parse(stop));
    }

    public boolean isTimeInInterval(Date time) {
        Calendar current = Calendar.getInstance();

        current.setTime(time);
        if (current.compareTo(stopTime) < 0) {
            current.add(Calendar.DATE, 1);
        }
        if (startTime.compareTo(stopTime) < 0) {
            startTime.add(Calendar.DATE, 1);
        }
        if (current.before(startTime)) {
            return false;
        } else {
            if (current.after(stopTime)) {
                stopTime.add(Calendar.DATE, 1);
            }
            return current.before(stopTime);
        }
    }

    public boolean equals(String ti) {
        return this.toString().equals(ti);
    }

    public boolean equals(TimeInterval ti) {
        return this.toString().equals(ti.toString()) && tags.equals(ti.getTags());
    }

    public Tags getTags() {
        return tags;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return String.format("[%02d:%02d, %02d:%02d]",
                startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE),
                stopTime.get(Calendar.HOUR_OF_DAY), stopTime.get(Calendar.MINUTE));
    }
}
