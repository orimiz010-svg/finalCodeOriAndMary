package org.example.calendarapp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalendarEntry {

    private String title;
    private String description;
    private LocalDate date;
    private int startHour;
    private int startMinute;
    private boolean startPM;
    private int endHour;
    private int endMinute;
    private boolean endPM;
    private String color;
    private static final ArrayList<CalendarEntry> entryList = new ArrayList<>();

    public CalendarEntry(String title, String description, LocalDate date,
                         int startHour, int startMinute, boolean startPM,
                         int endHour, int endMinute, boolean endPM,
                         String color) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.startPM = startPM;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.endPM = endPM;
        this.color = color;
    }

    public static void addEvent(CalendarEntry entry){ entryList.add(entry);}

    public static List<CalendarEntry> getEntryList(){
        return Collections.unmodifiableList(entryList);
    }

    public String toString(){
        return String.format("Title: %s\nDescription: %s", this.title, this.description);
    }

    public String getTitle()       { return title; }
    public String getDescription() { return description; }
    public LocalDate getDate()     { return date; }
    public int getStartHour()      { return startHour; }
    public int getStartMinute()    { return startMinute; }
    public boolean isStartPM()     { return startPM; }
    public int getEndHour()        { return endHour; }
    public int getEndMinute()      { return endMinute; }
    public boolean isEndPM()       { return endPM; }
    public String getColor()       { return color; }

    public String getTimeString() {
        String s1 = startHour + ":" + String.format("%02d", startMinute) + (startPM ? "PM" : "AM");
        String s2 = endHour   + ":" + String.format("%02d", endMinute)   + (endPM   ? "PM" : "AM");
        return s1 + " - " + s2;
    }
}
