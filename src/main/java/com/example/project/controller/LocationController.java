package com.example.project.controller;

public class LocationController
{
    private static double PI = 3.14159265;
    private static double EARTH_RADIUS = 6378137;
    private static double RAD = Math.PI / 180.0;

    // Calculate the distance (in metres) from the two latitude and longitude degrees provided
    public static double getDistance(double lng1, double lat1, double lng2, double lat2)
    {
        double radLat1 = lat1 * RAD;
        double radLat2 = lat2 * RAD;
        double a = radLat1 - radLat2;
        double b = (lng1 - lng2) * RAD;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    /**
     * Determining whether a point is within a circular area
     */
    public static boolean isInCircle(double lng1, double lat1, double lng2, double lat2, String radius) {
        double distance = getDistance(lng1, lat1, lng2,lat2);
        System.out.println(distance);
        double r = Double.parseDouble(radius);
        System.out.println(r);
        if (distance > r) {
            return false;
        } else {
            return true;
        }
    }

    // test
    public static void main(String[] args) {

        System.out.println(getDistance(116.407863, 39.914087, 116.413486,39.914336));
        boolean a=isInCircle(116.407863,39.914087,116.413486,39.914336,"500");
        System.out.println(a);
    }

}