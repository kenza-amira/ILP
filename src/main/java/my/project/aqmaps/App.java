package my.project.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class App {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// Starting point of our drone
		final var start = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));
		// Date input
		final var year = args[2];
		final var month = args[1];
		final var day = args[0];
		// We only need one HttpClient, shared between all HttpRequests
		final HttpClient client = HttpClient.newHttpClient();
		// Saving this string as it will be needed for multiple URL's.
		final String host = "http://localhost:" + args[6];
		// Initializing features list that we will need for our geojson output.
		var features = new ArrayList<Feature>();
		// Calling the classes to be able to use the methods later on.
		final var helper = new Helpers();
		final var search = new GraphSearch();
		final var sensHelp = new SensorHelpers();
		final var writer = new WriteToFile();

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
		String urlVisit = host + "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";

		var requestV = HttpRequest.newBuilder().uri(URI.create(urlVisit)).build();
		var responseV = client.send(requestV, BodyHandlers.ofString());

		Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
		ArrayList<Sensor> sensorList = new Gson().fromJson(responseV.body(), listType);

		/**
		 * Here we use GeoJson parsing to retrieve data about the no fly zones. Like
		 * before, we access information stored on the web server only this time it's in
		 * the buildings folder. Since it is a geoJSON string. We are able to easily
		 * retrieve the Feature Collection of our no fly zones. I've also created an
		 * ArrayList of LineString to store the LineStrings that make our Polygon. This 
		 * will be helpful later on for the path finding
		 */
		String urlDeadZone = host + "/buildings/no-fly-zones.geojson";
		var requestD = HttpRequest.newBuilder().uri(URI.create(urlDeadZone)).build();
		var responseD = client.send(requestD, BodyHandlers.ofString());
		var obstacles = FeatureCollection.fromJson(responseD.body());
		var noFly = obstacles.features();
		
		var allZones = new ArrayList<LineString>();
		for (Feature f: noFly) {
			var geo = f.geometry();
			var poly = (Polygon)geo;
			allZones.add(poly.outer());
		}


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
		
		var w3wAddress = new ArrayList<String>();
		var sensorsLocation = sensHelp.getSensorLoc(sensorList, host, client, start, w3wAddress);
		var batteries = sensHelp.getBatteries(sensorList);
		var readings = sensHelp.getReadings(sensorList);
		var lng = sensHelp.getLongitudes(sensorList, start, host, client);
		var lat = sensHelp.getLatitudes(sensorList, start, host, client);
		int length = sensorsLocation.size();

		//features.addAll(noFly);

		//Getting distances from all points to every other points
		double[][] dists = helper.generateDistanceMatrix(lng, lat, length);
		
		//Greedy search algorithm
		var route = search.greedySearch(dists, length, sensorsLocation);
		
		//Reordering our Sensors and their details in the order given by the greedy search (route).
		var orderedSensors = new ArrayList<Point>();
		var orderedReadings = new ArrayList<String>();
		var orderedBatteries= new ArrayList<String>();
		var w3wOrdered= new ArrayList<String>();
		helper.reorderArrays(route, sensorsLocation, batteries, readings, w3wAddress,
				orderedSensors, orderedBatteries, orderedReadings, w3wOrdered);
	
		// Path Finding algorithm
		var path = new ArrayList<String>();
		var lines = search.findPath(orderedSensors, orderedBatteries, orderedReadings, start, allZones, features, path, w3wOrdered);
		for (LineString l : lines) {
			features.add(Feature.fromGeometry((Geometry)l));
		}
		
		//Generating our map
		var collections = FeatureCollection.fromFeatures(features);
		var map = collections.toJson();
		System.out.println(collections.toJson());
		
		//Writing map into file
		var readingFilename = "readings-" + day + "-" + month + "-" + year + ".geojson";
		var outputFileReading = writer.createFile(readingFilename, map);
		System.out.println("File is at: " + outputFileReading.getAbsolutePath());
		
		//Writing flight path into file
		var flightpathFilename = "flightpath-" + day + "-" + month + "-" + year + ".txt";
		var outputFilePath = writer.writeLineByLine(flightpathFilename, path);
		System.out.println("File is at: " + outputFilePath.getAbsolutePath());
	}
	
	
}
