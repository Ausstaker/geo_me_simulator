package tech.tgo.snet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.RefEll;
import uk.me.jstott.jcoord.UTMRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Timothy Edge (timmyedge)
 */
public class ObservationTestHelpers {

    private static final Logger log = LoggerFactory.getLogger(ObservationTestHelpers.class);

    public static int SPEED_OF_LIGHT = 299792458; // [m/s]

    /*  Convert lat/lon to UTM northing/easting
    /*  - Filter operates in UTM format coords
    /*  - Returns Northing,Easting */
    public static double[] convertLatLngToUtmNthingEasting(double lat, double lng) {
        LatLng ltln = new LatLng(lat,lng);
        UTMRef utm = ltln.toUTMRef();
        return new double[]{utm.getNorthing(),utm.getEasting()};
    }

    public static double[] convertLatLngToUtmNthingEastingSpecificZone(double lat, double lng, char latZone, int lngZone) {
        UTMRef utm = toUTMRefSpecificZone(lat, lng, latZone, lngZone);
        return new double[]{utm.getNorthing(),utm.getEasting()};
    }

    public static Object[] getUtmLatZoneLonZone(double lat, double lng) {
        LatLng ltln = new LatLng(lat,lng);
        UTMRef utm = ltln.toUTMRef();
        return new Object[]{utm.getLatZone(),utm.getLngZone()};
    }

    public static double[] convertUtmNthingEastingToLatLng(double nthing, double easting, char latZone, int lngZone) {
        UTMRef utm = new UTMRef(nthing,easting,latZone,lngZone);
        LatLng ltln = utm.toLatLng();
        return new double[]{ltln.getLat(),ltln.getLng()};
    }

    public static double getRangeMeasurement(double a_y, double a_x, double true_y, double true_x, double range_rand_factor) {
        return Math.sqrt(Math.pow(a_y-true_y,2) + Math.pow(a_x-true_x,2)) + (Math.random()-0.5)*range_rand_factor;
    }

    public static double getTdoaMeasurement(double a_y, double a_x, double b_y, double b_x, double true_y, double true_x, double tdoa_rand_factor) {
        return (Math.sqrt(Math.pow(a_y-true_y,2) + Math.pow(a_x-true_x,2))
                - Math.sqrt(Math.pow(b_y-true_y,2) + Math.pow(b_x-true_x,2)))/SPEED_OF_LIGHT
                + (Math.random()-0.5)*tdoa_rand_factor;
    }

    public static double getAoaMeasurement(double a_y, double a_x, double true_y, double true_x, double aoa_rand_factor) {
        double meas_aoa = Math.atan((a_y-true_y)/(a_x-true_x)) + (Math.random()-0.5)*aoa_rand_factor;
        log.debug("Meas AOA: "+meas_aoa);

        if (true_x < a_x) {
            meas_aoa = meas_aoa + Math.PI;
        }
        if (true_y<a_y && true_x>=a_x) {
            meas_aoa = (Math.PI- Math.abs(meas_aoa)) + Math.PI;
        }
        log.debug("Meas AOA (adjusted): "+meas_aoa);
        return meas_aoa;
    }

    public static Object[] getMostPopularUTMZonesFromAssets(List<TestAsset> assets) {
        //List<Integer> lonZones = new ArrayList<Integer>();
        int[] lonZones = new int[assets.size()];
        char[] latZones = new char[assets.size()];
        for (int i=0; i<assets.size(); i++) {
            TestAsset asset = assets.get(i);
            int lonZone = (int) ObservationTestHelpers.getUtmLatZoneLonZone(asset.getCurrent_loc()[0], asset.getCurrent_loc()[1])[1];
            char latZone = (char) ObservationTestHelpers.getUtmLatZoneLonZone(asset.getCurrent_loc()[0], asset.getCurrent_loc()[1])[0];
            lonZones[i]=lonZone;
            latZones[i]=latZone;
        }
        return new Object[]{ObservationTestHelpers.getMode(latZones), ObservationTestHelpers.getMode(lonZones)};
    }

    public static int getMode(int[] array) {
        HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
        int max  = 1;
        int temp = array[0];
        for(int i = 0; i < array.length; i++) {
            if (hm.get(array[i]) != null) {
                int count = hm.get(array[i]);
                count++;
                hm.put(array[i], count);
                if(count > max) {
                    max  = count;
                    temp = array[i];
                }
            }
            else
                hm.put(array[i],1);
        }
        return temp;
    }

    public static char getMode(char[] array) {
        HashMap<Character,Integer> hm = new HashMap<Character, Integer>();
        int max  = 1;
        char temp = array[0];
        for(int i = 0; i < array.length; i++) {
            if (hm.get(array[i]) != null) {
                int count = hm.get(array[i]);
                count++;
                hm.put(array[i], count);
                if(count > max) {
                    max  = count;
                    temp = array[i];
                }
            }
            else
                hm.put(array[i],1);
        }
        return temp;
    }

    /* ADAPTED from uk.me.jstott.jcoord master method to allow projection onto custom UTM zones */
    public static UTMRef toUTMRefSpecificZone(double lat, double lon, char UTMZone, int longitudeZone) {
        double UTM_F0 = 0.9996D;
        double a = RefEll.WGS84.getMaj();
        double eSquared = RefEll.WGS84.getEcc();
        double longitude = lon;
        double latitude = lat;
        double latitudeRad = latitude * 0.017453292519943295D;
        double longitudeRad = longitude * 0.017453292519943295D;
        if (latitude >= 56.0D && latitude < 64.0D && longitude >= 3.0D && longitude < 12.0D) {
            longitudeZone = 32;
        }

        if (latitude >= 72.0D && latitude < 84.0D) {
            if (longitude >= 0.0D && longitude < 9.0D) {
                longitudeZone = 31;
            } else if (longitude >= 9.0D && longitude < 21.0D) {
                longitudeZone = 33;
            } else if (longitude >= 21.0D && longitude < 33.0D) {
                longitudeZone = 35;
            } else if (longitude >= 33.0D && longitude < 42.0D) {
                longitudeZone = 37;
            }
        }

        double longitudeOrigin = (double)((longitudeZone - 1) * 6 - 180 + 3);
        double longitudeOriginRad = longitudeOrigin * 0.017453292519943295D;
        double ePrimeSquared = eSquared / (1.0D - eSquared);
        double n = a / Math.sqrt(1.0D - eSquared * Math.sin(latitudeRad) * Math.sin(latitudeRad));
        double t = Math.tan(latitudeRad) * Math.tan(latitudeRad);
        double c = ePrimeSquared * Math.cos(latitudeRad) * Math.cos(latitudeRad);
        double A = Math.cos(latitudeRad) * (longitudeRad - longitudeOriginRad);
        double M = a * ((1.0D - eSquared / 4.0D - 3.0D * eSquared * eSquared / 64.0D - 5.0D * eSquared * eSquared * eSquared / 256.0D) * latitudeRad - (3.0D * eSquared / 8.0D + 3.0D * eSquared * eSquared / 32.0D + 45.0D * eSquared * eSquared * eSquared / 1024.0D) * Math.sin(2.0D * latitudeRad) + (15.0D * eSquared * eSquared / 256.0D + 45.0D * eSquared * eSquared * eSquared / 1024.0D) * Math.sin(4.0D * latitudeRad) - 35.0D * eSquared * eSquared * eSquared / 3072.0D * Math.sin(6.0D * latitudeRad));
        double UTMEasting = UTM_F0 * n * (A + (1.0D - t + c) * Math.pow(A, 3.0D) / 6.0D + (5.0D - 18.0D * t + t * t + 72.0D * c - 58.0D * ePrimeSquared) * Math.pow(A, 5.0D) / 120.0D) + 500000.0D;
        double UTMNorthing = UTM_F0 * (M + n * Math.tan(latitudeRad) * (A * A / 2.0D + (5.0D - t + 9.0D * c + 4.0D * c * c) * Math.pow(A, 4.0D) / 24.0D + (61.0D - 58.0D * t + t * t + 600.0D * c - 330.0D * ePrimeSquared) * Math.pow(A, 6.0D) / 720.0D));
        if (latitude < 0.0D) {
            UTMNorthing += 1.0E7D;
        }

        return new UTMRef(UTMEasting, UTMNorthing, UTMZone, longitudeZone);
    }
}
