package my.project.aqmaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import java.awt.geom.Line2D;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class GraphSearch {
	
	public ArrayList<Integer> greedySearch (double[][] dists, int length, ArrayList<Point> sensorsLocation) {
		var visited = new ArrayList<Integer>();
		var route = new ArrayList<Integer>();
		int counter = 1;
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(0);
		route.add(0);
		var helper = new Helpers();
		while (!queue.isEmpty() && counter < length) {
			counter += 1;
			Integer i = queue.remove();
			visited.add(i);
			double[] next = dists[i];
			for (Integer x : visited) {
				next[x] = 0.0;
			}
			double value = Double.MAX_VALUE;
			for (Double d : next) {
				value = (d == 0) ? value : Math.min(value, d);
			}
			int index = helper.findIndex(next, value);
			queue.add(index);
			visited.add(index);
			route.add(index);
		}
		return route;
	}
	
	public ArrayList<Point> findNext(Point first){
		var points = new ArrayList<Point>();
		var lat = first.latitude();
		var lng = first.longitude();
		
		points.add(Point.fromLngLat(lng +.0003, lat));
		points.add(Point.fromLngLat(lng +.00029544, lat + 0.00005)); 
		points.add(Point.fromLngLat(lng +.00028191, lat + 0.00010261)); 
		points.add(Point.fromLngLat(lng +.00025981, lat + 0.00015));
		points.add(Point.fromLngLat(lng +.00022981, lat + 0.00019284)); 
		points.add(Point.fromLngLat(lng +.00019284, lat +.00022981)); 
		points.add(Point.fromLngLat(lng +.00015, lat +.00025981)); 
		points.add(Point.fromLngLat(lng +.00010261, lat +.00028191));
		points.add(Point.fromLngLat(lng +.00005, lat +.00029544)); 
		points.add(Point.fromLngLat(lng , lat + .0003));
		
		points.add(Point.fromLngLat(lng -.0003, lat));
		points.add(Point.fromLngLat(lng -.00029544, lat + 0.00005));
		points.add(Point.fromLngLat(lng -.00028191, lat + 0.00010261));
		points.add(Point.fromLngLat(lng -.00025981, lat + 0.00015));
		points.add(Point.fromLngLat(lng -.00022981, lat + 0.00019284));
		points.add(Point.fromLngLat(lng -.00019284, lat +.00022981));
		points.add(Point.fromLngLat(lng -.00015, lat +.00025981));
		points.add(Point.fromLngLat(lng -.00010261, lat +.00028191));
		points.add(Point.fromLngLat(lng -.00005, lat +.00029544));
		
		points.add(Point.fromLngLat(lng -.00029544, lat - 0.00005));
		points.add(Point.fromLngLat(lng -.00028191, lat - 0.00010261));
		points.add(Point.fromLngLat(lng -.00025981, lat - 0.00015));
		points.add(Point.fromLngLat(lng -.00022981, lat - 0.00019284));
		points.add(Point.fromLngLat(lng -.00019284, lat -.00022981));
		points.add(Point.fromLngLat(lng -.00015, lat -.00025981));
		points.add(Point.fromLngLat(lng -.00010261, lat -.00028191));
		points.add(Point.fromLngLat(lng -.00005, lat -.00029544));
		points.add(Point.fromLngLat(lng , lat - .0003));
		
		points.add(Point.fromLngLat(lng + .00029544, lat - 0.00005));
		points.add(Point.fromLngLat(lng + .00028191, lat - 0.00010261));
		points.add(Point.fromLngLat(lng + .00025981, lat - 0.00015));
		points.add(Point.fromLngLat(lng + .00022981, lat - 0.00019284));
		points.add(Point.fromLngLat(lng + .00019284, lat -.00022981));
		points.add(Point.fromLngLat(lng + .00015, lat -.00025981));
		points.add(Point.fromLngLat(lng + .00010261, lat -.00028191));
		points.add(Point.fromLngLat(lng + .00005, lat -.00029544));
		
		return points;
	}	
	
	public boolean isIntersecting(LineString l, ArrayList<LineString> ls) {
		Point start = l.coordinates().get(0);
		Point end = l.coordinates().get(1);
		var x1 = start.longitude();
		var x2 = end.longitude();
		var y1 = start.latitude();
		var y2 = end.latitude();
		for (LineString s: ls) {
			for (int i = 0; i < s.coordinates().size()-1; i++) {
			Point start1 = s.coordinates().get(i);
			Point end1 = s.coordinates().get(i+1);
			var x3 = start1.longitude();
			var x4 = end1.longitude();
			var y3 = start1.latitude();
			var y4 = end1.latitude();
			if (Line2D.linesIntersect(x1,y1,x2,y2,x3,y3,x4,y4)) {
				return true;
			}
		}
		}
		return false;
	}
	
	public double slope(double x1, double y1, double x2, double y2) 
	{ 
	    return (y2 - y1) / (x2 - x1); 
	} 
	
	public ArrayList<LineString> findPath( ArrayList<Point> orderedSensors, ArrayList<String> orderedBatteries,
			ArrayList<String> orderedReadings, Point start, ArrayList<LineString>allZones, ArrayList<Feature> features){
		// Path Finding algorithm
				int sum = 0;
				var first = start;
				var lines = new ArrayList<LineString>();
				var helper = new Helpers();
				var sensHelp = new SensorHelpers();
				
				//Removing start from the arrays as we don't need it to be there for the pathfinding algorithm
				orderedSensors.remove(0);
				orderedBatteries.remove(0);
				orderedReadings.remove(0);
				
				//Added starting point to the end to attempt closed loop
				orderedSensors.add(start);
				orderedBatteries.add("0");
				orderedReadings.add("null");
				while (sum < 150 && !orderedSensors.isEmpty()) {
					var distance = new ArrayList<Double>();
					var points = new ArrayList<Point>();
					var nextPoints = findNext(first);
					var possible = new ArrayList<LineString>();
					var target = orderedSensors.get(0);
					for (Point p: nextPoints) {
						var temporaryPoints = new ArrayList<Point>();
						var x1 = (Double)p.latitude();
						var x2 = (Double)target.latitude();
						var y1 = (Double)p.longitude();
						var y2 = (Double)target.longitude();
						distance.add(helper.euclid(x1, y1, x2, y2));
						temporaryPoints.add(first);
						temporaryPoints.add(p);
						possible.add(LineString.fromLngLats(temporaryPoints));
					}
					var k = 0;
					for (LineString l: possible) {
						if (isIntersecting(l,allZones)) {
							distance.set(k, Double.MAX_VALUE);
						}
						k += 1;
					}
					int minIndex = distance.indexOf(Collections.min(distance));
					var nextP = nextPoints.get(minIndex);
					points.add(first);
					points.add(nextP);
					lines.add(LineString.fromLngLats(points));
					first = nextP;
					var feat = Feature.fromGeometry((Geometry)target);
					if (Collections.min(distance)<0.0002 && orderedSensors.size()!=1) {
						var coloredFeature = sensHelp.Color(orderedReadings.get(0), feat, orderedBatteries.get(0));
						orderedSensors.remove(0);
						orderedReadings.remove(0);
						orderedBatteries.remove(0);
						features.add(coloredFeature);
					}
					sum += 1;
				}
				return lines;
	}
	
}
