package tech.tgo.snet.util;

/**
 * @author Timothy Edge (timmyedge)
 */
public class TestTarget {

    String id;
    String name;

    /* TRUE LOCATION OF SIM TARGET */
    double true_lat;
    double true_lon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTrue_lat() {
        return true_lat;
    }

    public void setTrue_lat(double true_lat) {
        this.true_lat = true_lat;
    }

    public double getTrue_lon() {
        return true_lon;
    }

    public void setTrue_lon(double true_lon) {
        this.true_lon = true_lon;
    }
}
