package com.example.tanktoo.eventreportapp;

/**
 * Created by tanktoo on 15.07.2016.
 */


import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;



enum EventSourceType {
    USER("USER"),
    SENSOR("SENSOR");
    private String type;

    EventSourceType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}

enum EventType {
    PUBLIC_PARKING("PublicParking"),
    TRAFFIC_JAM("TrafficJam"),
    CONGESTION("Congestion");
    private String type;

    EventType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}

public class Event {
    private String identifier;
    private String eventClass;
    private String source;
    private int level;
    private double latitude;
    private double longitude;
    private String type;
    private Date date;

    public String getEventClass() {
        return eventClass;
    }

    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public String getStringDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return df.format(this.date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            this.date = df.parse(date);
        }catch (ParseException e){
            System.out.println("Exception while parsing date " + date + ": " + e.getMessage());
        }
    }

    public EventSourceType getEventSourceType(){
        String sourcetype = this.source.split("_")[0].toUpperCase();
        return EventSourceType.valueOf(sourcetype);
    }

    public EventType getEventType(){
        return EventType.valueOf(this.type);
    }

    public LatLng getLocation(){
        return new LatLng(this.latitude, this.longitude);
    }

}