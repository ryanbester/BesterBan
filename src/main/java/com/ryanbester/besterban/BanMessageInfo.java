package com.ryanbester.besterban;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Information for a ban message.
 */
public class BanMessageInfo {

    public String type;
    public String duration;
    public String reason;
    public String appealID;

    /**
     * Creates a new ban message information object.
     *
     * @param type     The type of ban.
     * @param duration The duration of the ban.
     * @param reason   The reason for the ban.
     * @param appealID The appeal ID.
     */
    public BanMessageInfo(String type, String duration, String reason, String appealID) {
        this.type = type;
        this.duration = duration;
        this.reason = reason;
        this.appealID = appealID;
    }

    /**
     * Converts a duration string to a human readable format.
     *
     * @param time The time.
     * @return The human readable string.
     */
    public static String parseTimeString(String time) {
        if (time.length() < 2) {
            return null;
        }

        try {
            int units = time.charAt(time.length() - 1);
            int actualTime = Integer.parseInt(time.substring(0, time.length() - 1));
            switch (units) {
                case 's':
                    // Seconds
                    return actualTime + " seconds";
                case 'm':
                    // Minutes
                    return actualTime + " minutes";
                case 'h':
                    // Hours
                    return actualTime + " hours";
                case 'd':
                    // Days
                    return actualTime + " days";
                case 'w':
                    // Weeks
                    return actualTime + " weeks";
                case 'f':
                    // Fortnights
                    return actualTime + " fortnights";
                case 'y':
                    // Years
                    return actualTime + " years";
                case 'c':
                    // Centuries
                    return actualTime + " centuries";
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets the expiry date object from the time string.
     *
     * @param time The time string.
     * @return The date object.
     */
    public static LocalDateTime getExpiryDate(String time) {
        if (time.length() < 2) {
            return null;
        }

        LocalDateTime dateStart = LocalDateTime.now();

        try {
            int units = time.charAt(time.length() - 1);
            int actualTime = Integer.parseInt(time.substring(0, time.length() - 1));
            switch (units) {
                case 's':
                    // Seconds
                    return dateStart.plusSeconds(actualTime);
                case 'm':
                    // Minutes
                    return dateStart.plusMinutes(actualTime);
                case 'h':
                    // Hours
                    return dateStart.plusHours(actualTime);
                case 'd':
                    // Days
                    return dateStart.plusDays(actualTime);
                case 'w':
                    // Weeks
                    return dateStart.plusWeeks(actualTime);
                case 'f':
                    // Fortnights
                    return dateStart.plusWeeks(actualTime * 2);
                case 'y':
                    // Years
                    return dateStart.plusYears(actualTime);
                case 'c':
                    // Centuries
                    return dateStart.plusYears(actualTime * 100);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts the time period to a human readable string.
     *
     * @param duration The time period.
     * @return The human readable string.
     */
    public static String getPeriodString(Duration duration) {
        if (duration.toDays() > 0) {
            return duration.toDays() + " days";
        }
        if (duration.toHours() > 0) {
            return duration.toHours() + " hours";
        }
        if (duration.toMinutes() > 0) {
            return duration.toMinutes() + " minutes";
        }
        if (duration.getSeconds() > 0) {
            return duration.getSeconds() + " seconds";
        }
        return "0 seconds";
    }

}
