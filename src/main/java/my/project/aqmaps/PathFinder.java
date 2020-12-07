package my.project.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

/**
 * PathFinder Class Description
 * 
 * This class contains all the method to help the drone fly and visit all
 * Sensors.
 *
 */
public class PathFinder extends SensorHelpers implements Helpers {
	private Point start;
	private ArrayList<Point> orderedSensors;
	private ArrayList<String> orderedReadings;
	private ArrayList<String> w3wOrdered;
	private ArrayList<String> orderedBatteries;
	private ArrayList<LineString> allZones;
	private long seed;

	/**
	 * This class constructor takes multiple arguments
	 * 
	 * @param start            The starting point of the drone (GeoJson Point)
	 * @param orderedSensors   The list of sensors that the drone needs to visit
	 *                         ordered (GeoJson Point)
	 * @param orderedReadings  The ordered list of air quality data (String)
	 * @param w3wOrdered       The ordered list of What3Words addresses (String)
	 * @param orderedBatteries The ordered list of battery readings (String)
	 * @param allZones         The list of LineString that constitute the no fly
	 *                         zones (GeoJson LineString)
	 * @param seed             For controlled randomness (long)
	 */
	public PathFinder(Point start, ArrayList<Point> orderedSensors, ArrayList<String> orderedReadings,
			ArrayList<String> w3wOrdered, ArrayList<String> orderedBatteries, ArrayList<LineString> allZones,
			long seed) {
		this.start = start;
		this.orderedBatteries = orderedBatteries;
		this.orderedSensors = orderedSensors;
		this.allZones = allZones;
		this.seed = seed;
		this.orderedReadings = orderedReadings;
		this.w3wOrdered = w3wOrdered;
	}

	/**
	 * This method builds the path from the starting point back to the starting
	 * point while taking readings from all the sensors
	 * 
	 * @param features This parameter is taken so that we can add the Points marker
	 *                 to the feature collection for our GeoJson Map (GeoJson
	 *                 Feature)
	 * @param path     This parameter is taken to output the flight path (String)
	 * @return This method returns all the LineStrings needed to do an entire loop
	 *         (GeoJson LineString)
	 */
	public ArrayList<LineString> findPath(ArrayList<Feature> features, ArrayList<String> path) {

		int moves = 0;
		var lines = new ArrayList<LineString>();

		// We start by initializing current to the starting point of the drone
		Point current = Point.fromLngLat(start.longitude(), start.latitude());

		// Keeping track of already visited LineStrings
		var visitedLineStrings = new ArrayList<LineString>();

		// Added starting point to the end to form closed loop
		orderedSensors.add(start);

		// The algorithm keeps going unless it visited all sensors or has reached the
		// moves limit
		while (moves < 150 && !orderedSensors.isEmpty()) {

			/*
			 * This String Builder is created in order to be able to produce the flight path
			 * documents later on. We use a String Builder to avoid concatenation
			 */
			StringBuilder st = new StringBuilder();
			st.append(String.valueOf(moves + 1) + ",");

			var distance = new ArrayList<Double>();
			var points = new ArrayList<Point>();

			// We find the list of Points we can reach from our current point
			var nextPoints = findNext(current);
			var possible = new ArrayList<LineString>();

			// We set the target which is the sensor we want to reach
			var target = orderedSensors.get(0);

			/*
			 * We loop through the possible next Points and compute the distance from them
			 * to the target We also make sure to keep track of all the possible LineStrings
			 */
			for (Point p : nextPoints) {
				var temporaryPoints = new ArrayList<Point>();
				var x1 = (Double) p.longitude();
				var x2 = (Double) target.longitude();
				var y1 = (Double) p.latitude();
				var y2 = (Double) target.latitude();
				distance.add(euclid(x1, y1, x2, y2));
				temporaryPoints.add(current);
				temporaryPoints.add(p);
				possible.add(LineString.fromLngLats(temporaryPoints));
			}
			/*
			 * If the LineString or the point is already visited or if it is intersecting
			 * with a polygon, we set the distance to the max value so that the algorithm
			 * never picks it
			 */
			for (int k = 0; k < possible.size(); k++) {

				if (visitedLineStrings.contains(possible.get(k))) {
					distance.set(k, Double.MAX_VALUE);
				} else if (isIntersecting(possible.get(k), allZones)) {
					distance.set(k, Double.MAX_VALUE);
				}
			}

			/*
			 * We get the indexes of all occurrences of the minimum value. We then use
			 * controlled randomness to decide which point (nextP) we choose
			 */
			var indexes = indexOfAll(Collections.min(distance), distance);
			Random randomizer = new Random(seed);

			// Note: this index *10 will correspond to the degree (between 0 and 360)
			var minIndex = indexes.get(randomizer.nextInt(indexes.size()));
			var nextP = nextPoints.get(minIndex);

			/*
			 * We create a LineString between our current point and our next point and add
			 * it to the ArrayList we wish to return. We also add it to the
			 * visitedLineStrings
			 */
			points.add(current);
			points.add(nextP);
			lines.add(LineString.fromLngLats(points));
			visitedLineStrings.add(LineString.fromLngLats(points));

			// We set current point to be the point we chose to go to
			current = nextP;

			st.append(String.valueOf(current.longitude()) + "," + String.valueOf(current.latitude()) + ","
					+ String.valueOf(minIndex * 10) + "," + String.valueOf(nextP.longitude()) + ","
					+ String.valueOf(nextP.latitude()) + ",");

			var feat = Feature.fromGeometry((Geometry) target);
			/*
			 * If the distance is less than 0.0003 and the sensor we are trying to reach is
			 * the start that means our loop is complete so we can stop the algorithm.
			 * 
			 * Otherwise if the distance is less than 0.0002 degrees than we color our
			 * feature, add it to our least of features and remove the first element of all
			 * the ArrayList
			 * 
			 * But if it's none of the above then we continue going without making any
			 * change
			 */
			if (Collections.min(distance) < 0.0003 && orderedSensors.get(0) == start) {
				st.append("null");
				path.add(st.toString());
				System.out.println("Moves: " + moves);
				return lines;
			} else if (Collections.min(distance) < 0.0002) {
				var coloredFeature = Color(orderedReadings.get(0), feat, orderedBatteries.get(0), w3wOrdered.get(0));
				st.append(String.valueOf(w3wOrdered.get(0)));
				orderedSensors.remove(0);
				orderedReadings.remove(0);
				orderedBatteries.remove(0);
				w3wOrdered.remove(0);
				features.add(coloredFeature);
			} else {
				st.append("null");
			}

			// We increase the number of moves
			moves += 1;
			path.add(st.toString());
		}
		System.out.println("150 reached");
		return lines;
	}

	/**
	 * This method finds the next possible points given a starting point. The drone
	 * cannot fly in an arbitrary direction. It can only be sent in a direction
	 * which is a multiple of ten degrees where by convention 0 means East, 90 means
	 * North, 180 means West and 270 means South.
	 * 
	 * @param current The starting point (GeoJson Point)
	 * @return An ArrayList with all the possible next points (GeoJson Point)
	 */
	public ArrayList<Point> findNext(Point current) {
		var points = new ArrayList<Point>();
		var lat = current.latitude();
		var lng = current.longitude();
		/*
		 * Knowing that for each move the drone travels a distance t, which is 0.0003
		 * degrees, we can calculate the drone's next position using simple planar
		 * trigonometry. The code below implements a unit circle and finds all 36
		 * options.
		 */
		for (int angle = 0; angle < 350; angle += 10) {
			var lngAdd = 0.0003 * Math.cos((Math.toRadians(angle)));
			var latAdd = 0.0003 * Math.sin((Math.toRadians(angle)));
			points.add(Point.fromLngLat(lng + lngAdd, lat + latAdd));
		}
		return points;
	}

	/**
	 * This method is checking for intersections between a LineString and a
	 * collection of LineStrings.
	 * 
	 * @param l  The LineString that we are interested in
	 * @param ls The collection of LineString that we check against
	 * @return A boolean. True if it is intersecting and False if it is not.
	 */
	public boolean isIntersecting(LineString l, ArrayList<LineString> ls) {
		Point start1 = l.coordinates().get(0);
		Point end1 = l.coordinates().get(1);

		var x1 = start1.longitude();
		var y1 = start1.latitude();

		var x2 = end1.longitude();
		var y2 = end1.latitude();

		// Checking for intersection for every LineString. We use the Java Line2D API.
		for (LineString s : ls) {
			for (int i = 0; i < s.coordinates().size() - 1; i++) {
				Point start2 = s.coordinates().get(i);
				Point end2 = s.coordinates().get(i + 1);
				var x3 = start2.longitude();
				var x4 = end2.longitude();
				var y3 = start2.latitude();
				var y4 = end2.latitude();

				if (Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
					return true;
				}
			}
		}
		return false;
	}
}
