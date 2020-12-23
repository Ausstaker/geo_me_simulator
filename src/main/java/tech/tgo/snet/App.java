package tech.tgo.snet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tgo.snet.util.ConfigurationException;
import tech.tgo.snet.util.TestAsset;
import tech.tgo.snet.util.TestTarget;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

/**
 * Simulate new observations following target
 * @author Timothy Edge (timmyedge)
 */
public class App 
{
    private static final Logger log = LoggerFactory.getLogger(App.class);

    SimulatedTargetObserver simulatedTargetObserver = new SimulatedTargetObserver();

    public static void main( String[] args ) {
        boolean run_ok=false;
        String feedback="";
        App app = new App();
        try {
            app.runit();
            log.info("Completed geo_me_simulator run ok. Check output.json");
            run_ok=true;
        }
        catch (ClassCastException cce) {
            cce.printStackTrace();
            feedback = "Class Cast Exception. Check all data types within input.json are correct, including:\n" +
                    "- all mandatory fields are included\n" +
                    "- all numbers are written as double types with .0 added to any round numbers\n" +
                    "- all booleans are either true or false\n" +
                    "- all strings are strings encapsulated by double quotes";
        }
        catch (ConfigurationException ce) {
            ce.printStackTrace();
            feedback = "Configuration Exception: "+ce.getMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
            feedback = "unknown error: "+e.getMessage();
        }

        if (!run_ok) {
            try {
                FileWriter file = new FileWriter("output.json");
                file.write("Problem with request: " + feedback);
                file.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /*
     * Processes input.json specifying true target location and a set of known asset locations
     * Generates output.json containing observations in format required by the Geo-me snet service
     */
    public void runit() throws Exception {
        Map<String,TestAsset> testAssets = new HashMap<String,TestAsset>();
        Map<String,TestTarget> testTargets = new HashMap<String,TestTarget>();

        InputStream is = new FileInputStream(new File("input.json"));
        if (is == null) {
            throw new ConfigurationException("Cannot find input config file");
        }
        JSONTokener tokener = new JSONTokener(is);
        JSONObject json_config = new JSONObject(tokener);
        JSONArray assets = json_config.getJSONArray("assets");

        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = (JSONObject) assets.get(i);
            log.debug(asset.toString());
            TestAsset ta = new TestAsset();
            ta.setId(asset.get("id").toString());
            JSONArray current_loc = (JSONArray) asset.get("current_loc");
            double lat = (Double) current_loc.get(0);
            double lon = (Double) current_loc.get(1);
            ta.setCurrent_loc(new double[]{lat, lon});
            ta.setProvide_aoa((Boolean) asset.get("provide_aoa"));
            ta.setProvide_range((Boolean) asset.get("provide_range"));
            testAssets.put(ta.getId(), ta);
        }

        JSONObject target = (JSONObject) json_config.get("target");
        TestTarget testTarget = new TestTarget();
        testTarget.setId(target.get("id").toString());
        testTarget.setName(target.get("name").toString());
        testTarget.setTrue_lat((double) target.get("true_lat"));
        testTarget.setTrue_lon((double) target.get("true_lon"));
        testTargets.put(testTarget.getId(), testTarget);

        /* Guide: 50 [m] */
        double range_rand_factor = (Double) json_config.get("range_rand_factor");

        /* Guide: 0.1 [radians] */
        double aoa_rand_factor = (Double) json_config.get("aoa_rand_factor");

        simulatedTargetObserver.setTestAssets(testAssets);
        simulatedTargetObserver.setTestTargets(testTargets);
        simulatedTargetObserver.setAoa_rand_factor(aoa_rand_factor);
        simulatedTargetObserver.setRange_rand_factor(range_rand_factor);

        FutureTask<JSONObject> future = new FutureTask<JSONObject>(simulatedTargetObserver);
        future.run();

        JSONObject simulation_scenario = future.get();
        log.debug("Result: "+simulation_scenario.toString());

        FileWriter file = new FileWriter("output.json");
        file.write(simulation_scenario.toString(4));
        file.close();
    }
}
