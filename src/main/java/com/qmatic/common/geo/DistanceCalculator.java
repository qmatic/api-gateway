package com.qmatic.common.geo;

/**
 * This class calculates distances.
 * 
 * @author Johan Grönvall (johan.gronvall@cybercomgroup.com)
 */
public class DistanceCalculator {

   private static final double EARTH_RADIUS = 3958.75;
   private static final double METER_CONVERSION = 1609;

   /**
    * Determines if two sets of coordinates is within each others radius range.
    * Uses the Haversine formula
    * (http://en.wikipedia.org/wiki/Haversine_formula).
    * 
    * @param longitude1
    *           The latitude of coordinate 1
    * @param latitude1
    *           The longitude of coordinate 1
    * @param longitude2
    *           The longitude of coordinate 2
    * @param latitude2
    *           The longitude of coordinate 2
    * @param radius
    *           The radius in meters.
    * @return true if in range, false otherwise
    */
   public boolean inRange(double long1, double lat1, double long2, double lat2,
         int radius) {
      return getDistance(long1, lat1, long2, lat2) < radius;
   }

   /**
    * Returns the distance between two points.
    * 
    * @param long1
    *           Longitude of point 1.
    * @param lat1
    *           Latitude of point 1.
    * @param long2
    *           Longitude of point 2.
    * @param lat2
    *           Latitude of point 2.
    * @return The distance in meters.
    */
   public double getDistance(double long1, double lat1, double long2,
         double lat2) {
      double longitude1 = long1;
      double latitude1 = lat1;
      double longitude2 = long2;
      double latitude2 = lat2;

      double dLat = Math.toRadians(latitude2 - latitude1);
      double dLng = Math.toRadians(longitude2 - longitude1);
      double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(latitude1))
            * Math.cos(Math.toRadians(latitude2)) * Math.sin(dLng / 2)
            * Math.sin(dLng / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

      return EARTH_RADIUS * c * METER_CONVERSION;
   }
}
