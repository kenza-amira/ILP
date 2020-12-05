package my.project.aqmaps;

import java.io.IOException;
import java.util.ArrayList;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class App implements Helpers{
	/**
	 * This main method uses the methods from the other classes to output the a text
	 * file containging the flightpath and a .geojson file containing the map. More
	 * information on the methods can be found in the other classes.
	 * 
	 * To run this method using the jar command please cd to the target directory
	 * then enter: java -jar aqmaps-0.0.1-SNAPSHOT.jar day month year lat lng seed
	 * port
	 * 
	 * @param args The inputs are day, month, year, starting point latitude,
	 *             starting point longitude, seed and port
	 * @throws IOException          If there's any failure in reading or writing a
	 *                              file an IOException will be thrown
	 * @throws InterruptedException If there's any failure in requests.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		// Starting point of our drone
		final var start = Point.fromLngLat(Double.parseDouble(args[4]), Double.parseDouble(args[3]));

		// Date input
		final var year = args[2];
		final var month = args[1];
		final var day = args[0];

		// Seed for controlled randomness
		final var seed = Long.parseLong(args[6]);

		// Saving this string as it will be needed for multiple URL's.
		final String host = "http://localhost:" + args[6];

		// Initializing features list that we will need for our geojson output.
		var features = new ArrayList<Feature>();

		// Calling the classes to be able to use the methods later on.
		final var writer = new WriteToFile();
		final var reader = new ServerReader(host, month, day, year);

		ArrayList<Sensor> sensorList = reader.getSensorList();
		ArrayList<LineString> allZones = reader.getNoFlyZones();

		final var sensHelp = new SensorHelpers(sensorList, host);
		var app = new App();

		/**
		 * We have previously formed an array of Sensor objects. Since we now have a
		 * Sensor class we are able to retrieve specific details about our sensors. In
		 * this step, we are interested in the location, readings and battery. We get
		 * the location which is a What3Words address. But in order to create points for
		 * our map we need the longitude and latitude associated with this address. We
		 * access the words folder in our web server to find this information and create
		 * a list of points for our map (sensorsLocation). We also keep track of the
		 * battery and readings. For purposes that we will see later on we separate the
		 * lng and lat into two arraylists
		 */

		var w3wAddress = new ArrayList<String>();
		var sensorsLocation = sensHelp.getSensorLoc(w3wAddress);
		var batteries = sensHelp.getBatteries();
		var readings = sensHelp.getReadings();
		var lng = sensHelp.getLongitudes();
		var lat = sensHelp.getLatitudes();
		int length = sensorsLocation.size();


		// Getting distances from all points to every other points
		double[][] dists = app.generateDistanceMatrix(lng, lat, length);


		// Two-Opt Algorithm
		final var search = new GraphSearch(sensorsLocation, start);

		var route = search.TwoOpt(dists);
		
		// Reordering our Sensors and their details in the order given by the Two-Opt search (route).
		var orderedSensors = new ArrayList<Point>();
		var orderedReadings = new ArrayList<String>();
		var orderedBatteries = new ArrayList<String>();
		var w3wOrdered = new ArrayList<String>();
		
		
		app.reorderArrays(route, sensorsLocation, batteries, readings, w3wAddress, orderedSensors, orderedBatteries,
				orderedReadings, w3wOrdered);

		// Path Finding algorithm
		var pathfinder = new PathFinder(start, orderedSensors, orderedReadings, w3wOrdered, orderedBatteries, 
				allZones, seed);
		var path = new ArrayList<String>();
		var lines = pathfinder.findPath(features, path);
		for (LineString l : lines) {
			features.add(Feature.fromGeometry((Geometry) l));
		}

		// Generating our map
		var collections = FeatureCollection.fromFeatures(features);
		var map = collections.toJson();
		System.out.println(collections.toJson());

		// Writing map into file
		var readingFilename = "readings-" + day + "-" + month + "-" + year + ".geojson";
		var outputFileReading = writer.createFile(readingFilename, map);
		System.out.println("File is at: " + outputFileReading.getAbsolutePath());

		// Writing flight path into file
		var flightpathFilename = "flightpath-" + day + "-" + month + "-" + year + ".txt";
		var outputFilePath = writer.writeLineByLine(flightpathFilename, path);
		System.out.println("File is at: " + outputFilePath.getAbsolutePath());
	}

}
