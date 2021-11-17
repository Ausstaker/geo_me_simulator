package tech.tgo.snet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tgo.snet.util.ObservationTestHelpers;
import tech.tgo.snet.util.ObservationType;
import tech.tgo.snet.util.TestAsset;
import tech.tgo.snet.util.TestTarget;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Simulate new observations following target
 * @author edge2ipi (https://github.com/Ausstaker)
 */
public class SimulatedTargetObserver implements Callable<JSONObject> {

    private static final Logger log = LoggerFactory.getLogger(SimulatedTargetObserver.class);

    Map<String,TestAsset> testAssets = new HashMap<String,TestAsset>();

    Map<String,TestTarget> testTargets = new HashMap<String,TestTarget>();

    double range_rand_factor; /* Guide: 50 [m] */
    double tdoa_rand_factor; /* Guide: 0.0000001 [sec] */
    double aoa_rand_factor; /* Guide: 0.1 [radians] */

    Map<String,Long> assetToObservationIdMapping = new HashMap<String,Long>();

    @Override
    public JSONObject call() throws Exception {

        JSONObject simulation_scenario = new JSONObject();
        JSONArray observations = new JSONArray();

        List<TestAsset> testAssetsList = testAssets.values().stream().collect(Collectors.toList());
        Object[] zones = ObservationTestHelpers.getMostPopularUTMZonesFromAssets(testAssetsList);
        int mostPopularLonZone = (int)zones[1];
        char mostPopularLatZone = (char)zones[0];
        log.debug("Most popular Lat Zone: "+mostPopularLatZone+", Lon Zone: "+mostPopularLonZone);

        for (TestTarget testTarget :testTargets.values()) {
            double[] utm_coords = ObservationTestHelpers.convertLatLngToUtmNthingEastingSpecificZone(testTarget.getTrue_lat(), testTarget.getTrue_lon(), mostPopularLatZone, mostPopularLonZone);
            double true_y = utm_coords[0];
            double true_x = utm_coords[1];

            /* for each asset, generate relevant observations */
            log.debug("Generating observations from # assets: " + testAssets.keySet().size());
            for (TestAsset asset : testAssets.values()) {
                utm_coords = ObservationTestHelpers.convertLatLngToUtmNthingEastingSpecificZone(asset.getCurrent_loc()[0], asset.getCurrent_loc()[1], mostPopularLatZone, mostPopularLonZone);
                double asset_y = utm_coords[0];
                double asset_x = utm_coords[1];

                if (asset.getProvide_range() != null && asset.getProvide_range()) {
                    Long obsId = assetToObservationIdMapping.get(asset.getId() + "_" + ObservationType.range.name() + "_" + testTarget.getId());
                    if (obsId == null) {
                        obsId = new Random().nextLong();
                        assetToObservationIdMapping.put(asset.getId() + "_" + ObservationType.range.name() + "_" + testTarget.getId(), obsId);
                    }
                    double meas_range = ObservationTestHelpers.getRangeMeasurement(asset_y, asset_x, true_y, true_x, range_rand_factor);
                    log.debug("Asset: " + asset.getId() + ", Meas range: " + meas_range);

                    JSONObject obs = new JSONObject();
                    obs.put("id", new Random().nextLong());
                    obs.put("type", ObservationType.range.name());
                    obs.put("meas", meas_range);
                    obs.put("assetId", asset.getId());
                    obs.put("lat", asset.getCurrent_loc()[0]);
                    obs.put("lon", asset.getCurrent_loc()[1]);
                    observations.put(obs);
                }

                if (asset.getProvide_aoa() != null && asset.getProvide_aoa()) {
                    Long obsId = assetToObservationIdMapping.get(asset.getId() + "_" + ObservationType.aoa.name() + "_" + testTarget.getId());
                    if (obsId == null) {
                        obsId = new Random().nextLong();
                        assetToObservationIdMapping.put(asset.getId() + "_" + ObservationType.aoa.name() + "_" + testTarget.getId(), obsId);
                    }
                    double meas_aoa = ObservationTestHelpers.getAoaMeasurement(asset_y, asset_x, true_y, true_x, aoa_rand_factor);
                    log.debug("Asset: " + asset.getId() + ", Meas AOA: " + meas_aoa);

                    JSONObject obs = new JSONObject();
                    obs.put("id", new Random().nextLong());
                    obs.put("type", ObservationType.aoa.name());
                    obs.put("meas", meas_aoa);
                    obs.put("assetId", asset.getId());
                    obs.put("lat", asset.getCurrent_loc()[0]);
                    obs.put("lon", asset.getCurrent_loc()[1]);
                    observations.put(obs);
                }
                log.debug("number obs: "+observations.length());
            }
        }
        log.debug("number obs: "+observations.length());

        simulation_scenario.put("observation", observations);
        for (TestTarget testTarget :testTargets.values()) {
            JSONObject target = new JSONObject();
            target.put("id", testTarget.getId());
            target.put("name", testTarget.getName());
            simulation_scenario.put("target", target);
        }
        simulation_scenario.put("provide_kml_out", true);
        return simulation_scenario;
    }

    public double getRange_rand_factor() {
        return range_rand_factor;
    }

    public void setRange_rand_factor(double range_rand_factor) {
        this.range_rand_factor = range_rand_factor;
    }

    public double getTdoa_rand_factor() {
        return tdoa_rand_factor;
    }

    public void setTdoa_rand_factor(double tdoa_rand_factor) {
        this.tdoa_rand_factor = tdoa_rand_factor;
    }

    public double getAoa_rand_factor() {
        return aoa_rand_factor;
    }

    public void setAoa_rand_factor(double aoa_rand_factor) {
        this.aoa_rand_factor = aoa_rand_factor;
    }

    public Map<String, TestAsset> getTestAssets() {
        return testAssets;
    }

    public void setTestAssets(Map<String, TestAsset> testAssets) {
        this.testAssets = testAssets;
    }

    public Map<String, TestTarget> getTestTargets() {
        return testTargets;
    }

    public void setTestTargets(Map<String, TestTarget> testTargets) {
        this.testTargets = testTargets;
    }

}
