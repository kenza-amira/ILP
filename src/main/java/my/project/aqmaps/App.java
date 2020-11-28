package my.project.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException, InterruptedException {
		// Starting point of our drone
		var start = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
		System.out.println(start);
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

		Type listType = new TypeToken<ArrayList<Sensor>>() {
		}.getType();
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
		var fc = FeatureCollection.fromJson(responseD.body());
		var noFly = fc.features();
		// System.out.println(responseV.body());
		// System.out.println(responseD.body());

		/**
		 * We have previously formed an array of Sensor objects. Since we now have a
		 * Sensor class we are able to retrieve specific details about our sensors. In
		 * this step, we are interested in the location. We get the location which is a
		 * What3Words address. But in order to create points for our map we need the
		 * longitude and latitude associated with this address. We access the words
		 * folder in our web server to find this information and create a list of points
		 * for our map.
		 */
		var sensorsLocation = new ArrayList<Point>();
		var batteries = new ArrayList<String>();
		var readings = new ArrayList<String>();

		var lng = new ArrayList<Double>();
		var lat = new ArrayList<Double>();

		sensorsLocation.add(start);
		lng.add(start.longitude());
		lat.add(start.latitude());
		batteries.add("0");
		readings.add("NaN");

		int length = 0;
		for (Sensor sensor : sensorList) {
			// We changed the format of the location string to be able to fetch the data
			// from the server
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			// Like before, we use GSon Parsing to get a LocationDetails object that way we
			// can acess
			// the coordinates. The only difference is that we are deserializing a JSON
			// record instead
			// of a JSON list. Once we have the coordinates we create a geoJson Point.
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);
			var loc = Point.fromLngLat(details.coordinates.lng, details.coordinates.lat);
			batteries.add(sensor.getBattery());
			// System.out.println(sensor.getReading());
			readings.add(sensor.getReading());
			lng.add(details.coordinates.lng);
			lat.add(details.coordinates.lat);
			sensorsLocation.add(loc);
			length += 1;
		}

		int k = 0;
		for (Point p : sensorsLocation) {
			var feature = Feature.fromGeometry((Geometry) p);
			Color(readings.get(k), feature, batteries.get(k));
			features.add(feature);
			k += 1;
		}
		features.addAll(noFly);

		double[][] dists = new double[length][length];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				dists[i][j] = euclid(lng.get(i), lat.get(i), lng.get(j), lat.get(j));
				System.out.println(euclid(lng.get(i), lat.get(i), lng.get(j), lat.get(j)));
			}
		}
		// greedy search algorithm
		var visited = new ArrayList<Integer>();
		var route = new ArrayList<Integer>();
		int counter = 1;
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(0);
		route.add(0);
		double sum = 0;

		while (!queue.isEmpty() && counter < length && sum < 150 * 0.0003) {
			counter += 1;
			Integer i = queue.remove();
			// System.out.println(i);
			visited.add(i);
			double[] next = dists[i];
			for (Integer x : visited) {
				next[x] = 0.0;
			}
			double value = Double.MAX_VALUE;
			for (Double d : next) {
				value = (d == 0) ? value : Math.min(value, d);
			}
			// System.out.println(value);
			int index = findIndex(next, value);
			// System.out.println(index);
			queue.add(index);
			visited.add(index);
			route.add(index);
			sum += value;
		}

		var orderedSensors = new ArrayList<Point>();
		for (Integer i : route) {
			orderedSensors.add(sensorsLocation.get(i));
		}
		System.out.println(sensorsLocation.size());
		var ls = LineString.fromLngLats(orderedSensors);
		features.add(Feature.fromGeometry((Geometry) ls));

		// To have a closed loop eventually
		double tmp = euclid(orderedSensors.get(0).longitude(), orderedSensors.get(0).latitude(),
				orderedSensors.get(0).longitude(), orderedSensors.get(0).latitude());
		if (tmp + sum < 150 * 0.0003) {
			var tmpLs = new ArrayList<Point>();
			tmpLs.add(orderedSensors.get(0));
			tmpLs.add(orderedSensors.get(1));
			var Ls = LineString.fromLngLats(tmpLs);
			features.add(Feature.fromGeometry((Geometry) Ls));
		}

		var collections = FeatureCollection.fromFeatures(features);
		System.out.println(collections.toJson());
	}

	public static double euclid(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	public static int findIndex(double arr[], double value) {

		// if array is Null
		if (arr == null) {
			return -1;
		}

		// find length of array
		int len = arr.length;
		int i = 0;

		// traverse in the array
		while (i < len) {

			// if the i-th element is t
			// then return the index
			if (arr[i] == value) {
				return i;
			} else {
				i = i + 1;
			}
		}
		return -1;
	}

	public static void Color(String airQ, Feature feature, String battery) {
		if (!airQ.equals("null") && !airQ.equals("NaN")) {
			double airQuality = Double.parseDouble(airQ);
			if (Double.parseDouble(battery) > 10) {
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
			}
		} else {
			feature.addStringProperty("marker-color", "#000000");
			feature.addStringProperty("rgb-string", "#000000");
			feature.addStringProperty("marker-symbol", "cross");
		}
	}
}
