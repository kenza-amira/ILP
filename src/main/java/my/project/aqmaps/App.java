package my.project.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.*;


/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// Starting point of our drone
		var start = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
		// We only need one HttpClient, shared between all HttpRequests
		final HttpClient client = HttpClient.newHttpClient();
		// Saving this string as it will be needed for multiple URL's.
		final String host = "http://localhost:" + args[6];
		// Initializing features list that we will need for our geojson output.
		var features = new ArrayList<Feature>();
		// Using the 6th argument as the random number seed for the application.
		try {
			int seed = Integer.parseInt(args[5]);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("The seed entered, " + args[5] + " is invalid.", nfe);
		}
		
		var helper = new Helpers();
		var search = new GraphSearch();
		var sensHelp = new SensorHelpers();

		/**
		 * This part of the code fetches the information stored in the web server maps
		 * folder. It uses the first 3 arguments (day, month, year) to get the sensors
		 * that need to be visited on that date. It then uses Gson parsing to to
		 * deserialise the JSON list into an object of type ArrayList<Sensor>. Since We
		 * can’t get a class associated with ArrayList<Sensor> we get its Type using
		 * Java’s Reflection API which is used to examine or modify methods, classes, or
		 * interfaces at runtime. This allows use to have a Sensor Object (With
		 * location, battery and reading) and to have a List of sensors to visit on that
		 * date.
		 * 
		 */
		String urlVisit = host + "/maps/" + args[2] + "/" + args[1] + "/" + args[0] + "/air-quality-data.json";

		var requestV = HttpRequest.newBuilder().uri(URI.create(urlVisit)).build();
		var responseV = client.send(requestV, BodyHandlers.ofString());

		Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
		ArrayList<Sensor> sensorList = new Gson().fromJson(responseV.body(), listType);

		/**
		 * Here we use GeoJson parsing to retrieve data about the no fly zones. Like
		 * before, we access information stored on the web server only this time it's in
		 * the buildings folder. Since it is a geoJSON string. We are able to easily
		 * retrieve the Feature Collection of our no fly zones.
		 */
		String urlDeadZone = host + "/buildings/no-fly-zones.geojson";
		var requestD = HttpRequest.newBuilder().uri(URI.create(urlDeadZone)).build();
		var responseD = client.send(requestD, BodyHandlers.ofString());
		var obstacles = FeatureCollection.fromJson(responseD.body());
		var noFly = obstacles.features();

		/**
		 * We have previously formed an array of Sensor objects. Since we now have a
		 * Sensor class we are able to retrieve specific details about our sensors. In
		 * this step, we are interested in the location, readings and battery. We get the location which is a
		 * What3Words address. But in order to create points for our map we need the
		 * longitude and latitude associated with this address. We access the words
		 * folder in our web server to find this information and create a list of points
		 * for our map (sensorsLocation). We also keep track of the battery and readings.
		 * For purposes that we will see later on we separate the lng and lat into two arraylists
		 */
		

		var sensorsLocation = sensHelp.getSensorLoc(sensorList, host, client, start);
		var batteries = sensHelp.getBatteries(sensorList);
		var readings = sensHelp.getReadings(sensorList);
		var lng = sensHelp.getLongitudes(sensorList, start, host, client);
		var lat = sensHelp.getLatitudes(sensorList, start, host, client);
/*		for (Feature f: noFly) {
			var geometry = f.geometry();
			var poly = (Polygon)geometry;
			var coord = poly.coordinates();
			var flat = coord.stream()
			        .flatMap(List::stream)
			        .collect(Collectors.toList());
			for (Point p: flat) {
				sensorsLocation.add(p);
				var lt = p.latitude();
				var ln = p.longitude();
				lat.add(lt);
				lng.add(ln);
			}
		}*/
		int length = sensorsLocation.size();

//		int k = 0;
//		for (Point p : sensorsLocation) {
//			var feature = Feature.fromGeometry((Geometry) p);
//			Color(readings.get(k), feature, batteries.get(k));
//			features.add(feature);
//			k += 1;
//		}
		features.addAll(noFly);
		
		
		//Getting distances from all points to every other points
		double[][] dists = helper.generateDistanceMatrix(lng, lat, length);
		
		//Greedy search algorithm
		var orderedSensors = search.greedySearch(dists, length, sensorsLocation);
		var ls = LineString.fromLngLats(orderedSensors);
		features.add(Feature.fromGeometry((Geometry) ls));

		var collections = FeatureCollection.fromFeatures(features);
		System.out.println(collections.toJson());
	}


	public static void Color(String airQ, Feature feature, String battery) {
		if (!airQ.equals("null") && !airQ.equals("NaN") && 
				Double.parseDouble(battery) > 10) {
			double airQuality = Double.parseDouble(airQ);
				if (0 <= airQuality && airQuality < 32) {
					feature.addStringProperty("marker-color", "#00ff00");
					feature.addStringProperty("rgb-string", "#00ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (32 <= airQuality && airQuality < 64) {
					feature.addStringProperty("marker-color", "#40ff00");
					feature.addStringProperty("rgb-string", "#40ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (64 <= airQuality && airQuality < 96) {
					feature.addStringProperty("marker-color", "#80ff00");
					feature.addStringProperty("rgb-string", "#80ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (96 <= airQuality && airQuality < 128) {
					feature.addStringProperty("marker-color", "#c0ff00");
					feature.addStringProperty("rgb-string", "#c0ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (128 <= airQuality && airQuality < 160) {
					feature.addStringProperty("marker-color", "#ffc000");
					feature.addStringProperty("rgb-string", "#ffc000");
					feature.addStringProperty("marker-symbol", "danger");
				} else if (160 <= airQuality && airQuality < 192) {
					feature.addStringProperty("marker-color", "#ff8000");
					feature.addStringProperty("rgb-string", "#ff8000");
					feature.addStringProperty("marker-symbol", "danger");
				} else if (192 <= airQuality && airQuality < 224) {
					feature.addStringProperty("marker-color", "#ff4000");
					feature.addStringProperty("rgb-string", "#ff4000");
					feature.addStringProperty("marker-symbol", "danger");
				} else if (224 <= airQuality && airQuality < 256) {
					feature.addStringProperty("marker-color", "#ff0000");
					feature.addStringProperty("rgb-string", "#ff0000");
					feature.addStringProperty("marker-symbol", "danger");
				} else {
					// If the number fetched is not included int the previous cases we throw
					// an illegal argument excpetion.
					throw new IllegalArgumentException("Value out of bound (" + airQuality + ")");
				}
		} else if(airQ.equals("null") || airQ.equals("NaN") || 
				Double.parseDouble(battery) <= 10){
			feature.addStringProperty("marker-color", "#000000");
			feature.addStringProperty("rgb-string", "#000000");
			feature.addStringProperty("marker-symbol", "cross");
		} 
	}
}
