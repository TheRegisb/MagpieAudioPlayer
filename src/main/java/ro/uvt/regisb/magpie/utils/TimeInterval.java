/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.uvt.regisb.magpie.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Monitored time slot and its attributes.
 * Encompass an hour-minute time interval, its tags and their weight.
 *
 * @see Tags
 */
public class TimeInterval implements Serializable {
    private Calendar startTime = Calendar.getInstance();
    private Calendar stopTime = Calendar.getInstance();
    private Tags tags = new Tags();
    private transient boolean isActive = false;

    /**
     * Default constructor.
     * Create a new TimeInterval with empty tags.
     * Support days overlaps, e.g. 22:30 - 06:15.
     *
     * @param start Start of the time interval, as "HH:mm"
     * @param stop  End of the time interval. as "HH:mm"
     * @throws ParseException Arguments where not formatted as "HH:mm"
     */
    public TimeInterval(String start, String stop) throws ParseException {
        startTime.setTime(new SimpleDateFormat("HH:mm").parse(start));
        stopTime.setTime(new SimpleDateFormat("HH:mm").parse(stop));
    }

    /**
     * Copy constructor.
     * @param ti Instance to copy.
     */
    public TimeInterval(TimeInterval ti) {
        this.startTime = ti.startTime;
        this.stopTime = ti.stopTime;
        this.isActive = ti.isActive;
        this.tags = ti.tags;
    }

    /**
     * Check if a Date is in the interval.
     * @param time Date to check, whose day must be set to 1970-01-01
     * @return The inclusion of time in the interval.
     */
    public boolean isTimeInInterval(Date time) {
        Calendar current = Calendar.getInstance();
        // Offset are used to support day overlaps.
        boolean revertStart = false;
        boolean revertStop = false;
        boolean isIn = false;

        current.setTime(time);
        if (current.compareTo(stopTime) < 0) {
            current.add(Calendar.DATE, 1);
        }
        if (startTime.compareTo(stopTime) < 0) {
            startTime.add(Calendar.DATE, 1);
            revertStart = true;
        }
        if (!current.before(startTime)) {
            if (current.after(stopTime)) {
                stopTime.add(Calendar.DATE, 1);
                revertStop = true;
            }
            isIn = current.before(stopTime);
        }
        // As the instance's member where affected, the offset must be removed for subsequent checks.
        startTime.add(Calendar.DATE, (revertStart ? -1 : 0));
        stopTime.add(Calendar.DATE, (revertStop ? -1 : 0));
        return isIn;
    }

    /**
     * Compare a time interval literal to the instance interval.
     * @param ti A string formatted as "[HH:mm, HH:mm]".
     * @return The equivalence of both intervals.
     */
    public boolean equals(String ti) {
        return this.toString().equals(ti);
    }

    /**
     * Get tags.
     * @return Time slot's tags.
     * @see Tags
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Get time slot activity.
     * @return (1) true if the attributes of this instance are in effect or (2) false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Set time slot activity.
     * Enable or disable the attributes effects.
     * @param active Bound time slot activity.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Describe the instance in a human-readable fashion.
     * @return A string describing the value of each field of the instance.
     */
    @Override
    public String toString() {
        return String.format("[%02d:%02d, %02d:%02d]",
                startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE),
                stopTime.get(Calendar.HOUR_OF_DAY), stopTime.get(Calendar.MINUTE));
    }

    /**
     * Inverse the tags weight of the process.
     * @return A copy of the instance with the opposite tags weight.
     */
    public TimeInterval invert() {
        TimeInterval inverse = new TimeInterval(this);

        inverse.tags = inverse.tags.invert();
        return inverse;
    }
}
