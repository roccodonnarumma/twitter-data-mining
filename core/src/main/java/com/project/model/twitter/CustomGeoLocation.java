package com.project.model.twitter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Custom GeoLocation POJO.
 * 
 * @author rdonnarumma
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomGeoLocation implements Serializable {
    private static final long serialVersionUID = 2929227234059303893L;

    private double latitude;
    private double longitude;

    public CustomGeoLocation() {
    }

    public CustomGeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
