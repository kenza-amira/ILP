package my.project.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class PathFinder {
	private Point start;
	private ArrayList<Point> orderedSensors;
	private ArrayList<String> orderedReadings;
	private ArrayList<String> w3wOrdered;
	private ArrayList<String> orderedBatteries;
	private ArrayList<LineString> allZones;
	private long seed;
	
	public PathFinder (Point start, ArrayList<Point> orderedSensors, ArrayList<String> orderedReadings,
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

public ArrayList<LineString> findPath(ArrayList<Feature> features, ArrayList<String> path) {
		
		// Path Finding algorithm
		int sum = 0;
		Point first = Point.fromLngLat(start.longitude(), start.latitude());
		var lines = new ArrayList<LineString>();
		var visited = new ArrayList<LineString>();
		var visitedP = new ArrayList<Point>();
		var helper = new Helpers();
		var sensHelp = new SensorHelpers();

		// Added starting point to the end to form closed loop
		orderedSensors.add(start);
		
		while (sum < 150 && !orderedSensors.isEmpty()) {
			var st = String.valueOf(sum + 1) + ",";
			var distance = new ArrayList<Double>();
			var points = new ArrayList<Point>();
			var nextPoints = findNext(first);
			var possible = new ArrayList<LineString>();
			var target = orderedSensors.get(0);
			for (Point p : nextPoints) {
				var temporaryPoints = new ArrayList<Point>();
				var x1 = (Double) p.longitude();
				var x2 = (Double) target.longitude();
				var y1 = (Double) p.latitude();
				var y2 = (Double) target.latitude();
				distance.add(helper.euclid(x1, y1, x2, y2));
				temporaryPoints.add(first);
				temporaryPoints.add(p);
				possible.add(LineString.fromLngLats(temporaryPoints));
		}

			for (int k = 0; k< possible.size(); k++) {
				var tmp = possible.get(k).coordinates().get(0);

				if (visited.contains(possible.get(k))){
					distance.set(k, Double.MAX_VALUE);
					//visitedP.add(first);
					//visitedP.add(tmp);
				}
				else if (isIntersecting(possible.get(k), allZones)) {
					distance.set(k, Double.MAX_VALUE);
					//System.out.print(k+",");
				} else if(visitedP.contains(tmp)) {
					distance.set(k, Double.MAX_VALUE);
					//visitedP.add(tmp);
				}
			}
			
			var indexes = helper.indexOfAll(Collections.min(distance), distance);
			Random randomizer = new Random(seed);
			var minIndex = indexes.get(randomizer.nextInt(indexes.size()));
			var nextP = nextPoints.get(minIndex);
			
			points.add(first);
			points.add(nextP);
			st = st + String.valueOf(first.longitude()) + "," + String.valueOf(first.latitude()) + ","
					+ String.valueOf(minIndex * 10) + "," + String.valueOf(nextP.longitude()) + ","
					+ String.valueOf(nextP.latitude()) + ",";
			
			lines.add(LineString.fromLngLats(points));
			visited.add(LineString.fromLngLats(points));
			first = nextP;
			var feat = Feature.fromGeometry((Geometry) target);
			if (Collections.min(distance) < 0.0003 && orderedSensors.get(0) == start) {
				st += "null";
				path.add(st);
				System.out.println("Moves: "+ sum);
				return lines;
			} else if (Collections.min(distance) < 0.0002) {
				var coloredFeature = sensHelp.Color(orderedReadings.get(0), feat, 
						orderedBatteries.get(0),w3wOrdered.get(0));
				st = st + String.valueOf(w3wOrdered.get(0));
				orderedSensors.remove(0);
				orderedReadings.remove(0);
				orderedBatteries.remove(0);
				w3wOrdered.remove(0);
				features.add(coloredFeature);
			} else {
				st = st + "null";
			}
			
			sum += 1;
			path.add(st);
		}
		System.out.println("150 reached");
		//start = first;
		return lines;
	}

/**
 * This method finds the next possible points given a starting point. The drone
 * cannot fly in an arbitrary direction. It can only be sent in a direction
 * which is a multiple of ten degrees where by convention 0 means East, 90 means
 * North, 180 means West and 270 means South.
 * 
 * @param first The starting point (GeoJson Point)
 * @return An ArrayList with all the possible next points (GeoJson Point)
 */
public ArrayList<Point> findNext(Point first) {
	var points = new ArrayList<Point>();
	var lat = first.latitude();
	var lng = first.longitude();
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
	
	//Checking for intersection for every LineString. We use the Java Line2D API.
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
