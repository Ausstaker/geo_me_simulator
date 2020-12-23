# geo_me_simulator

# Description
This application simulates the location of a platform (the target) and a set of observations from the platform to a predefined set of landmarks (the asset(s)). The simulated data is intened to be fed into the Geo-me service. The observations are computed with a predetermined and modifiable random factor which adds a random error to the measurement. This error and resultant set of observations from each predefined asset enables easy and thorough testing of the Geo-me service under various scnearios.

# Quick Start - Generating Example Inputs for the SingularityNET Geo-me Service
1. navigate to standalone/
2. modify input.json according to the guidelines below, retain input.json in the same folder location as geo_me .jar file
3. run geo_me_simulator-1.0-SNAPSHOT-jar-with-dependencies.jar
   - Command line: java -jar geo_me_simulator-1.0-SNAPSHOT-jar-with-dependencies.jar
   - Desktop UI: double click

# Inputs - input.json
```
{
  "assets": [
    {
      "id": "<string>",
      "provide_aoa": <boolean>,
      "provide_range": <boolean>,
      "current_loc": [<double - latitude in decimal degrees -90 to 90>, <double - latitude in decimal degrees -180 to 180>]
    },
    ...
  ],
  "target":{
    "id":"<string>",
    "name":"<string>",
    "true_lat":<double - latitude in decimal degrees -90 to 90>,
    "true_lon":<double - latitude in decimal degrees -180 to 180>
  },
  "range_rand_factor": <double - [metres]//Guide 50m>,
  "aoa_rand_factor": <double - [radians]//Guide 0.1>
}
```

# Outputs - output.json
The following output of the simulator is intended as "input" to the SingularityNET Geo-me service
```
{
    "observation": [
        {
            "targetId": "<string>",
            "assetId": "<string>",
            "meas": <double - range in [metres] or angle in [radians] according to the type>,          
            "id": <int>,
            "type": "<'range' or 'aoa'>",
            "lat": <double - latitude in decimal degrees -90 to 90>,
            "lon": <double - latitude in decimal degrees -180 to 180>
        },
        ...
    ],
    "target": {
        "name": "<string>",
        "id": "<string>"
    }
}
```
