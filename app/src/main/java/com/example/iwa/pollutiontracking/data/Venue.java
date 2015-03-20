package com.example.iwa.pollutiontracking.data;

/**
 * Created by matteo on 20/03/15.
 */
public class Venue {
    private long id;
    private String uri;
    private String name;
    private String address;
    private float lat;
    private float lon;

    public Venue(long id, String uri, String name, String address, float lat, float lon) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public float getLatitude() {
        return lat;
    }

    public float getLongitude() {
        return lon;
    }
}
