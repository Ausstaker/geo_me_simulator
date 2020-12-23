package tech.tgo.snet.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 * @author Timothy Edge (timmyedge)
 */
public class ObservationTestHelpers {

    private static final Logger log = LoggerFactory.getLogger(ObservationTestHelpers.class);

    public static int SPEED_OF_LIGHT = 299792458; // [m/s]

    /*  Convert lat/lon to UTM northing/easting
    /*  - Filter operates in UTM format coords
    /*  - Returns Northing,Easting
    /*  - Example UTM coords: 6470194.755756934,403548.8617473827 */
    public static double[] convertLatLngToUtmNthingEasting(double lat, double lng) {
        LatLng ltln = new LatLng(lat,lng);
        UTMRef utm = ltln.toUTMRef();
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
}
