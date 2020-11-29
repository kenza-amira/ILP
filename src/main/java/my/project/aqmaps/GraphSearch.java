package my.project.aqmaps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class GraphSearch {
	
	public ArrayList<Integer> greedySearch (double[][] dists, int length, ArrayList<Point> sensorsLocation) {
		var visited = new ArrayList<Integer>();
		var route = new ArrayList<Integer>();
		int counter = 1;
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(0);
		route.add(0);
		var helper = new Helpers();
		//System.out.println(dists);
		while (!queue.isEmpty() && counter < length) {
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
			int index = helper.findIndex(next, value);
			// System.out.println(index);
			queue.add(index);
			visited.add(index);
			route.add(index);
		}
		//System.out.println(route.size());	
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
	
	public boolean Intersects(LineString ls, List<Polygon> polygons) {
		var helper = new Helpers();
		Boolean intersects = false;
		Point start = ls.coordinates().get(0);
		Point end = ls.coordinates().get(1);
		var x1 = start.longitude();
		var x2 = end.longitude();
		var y1 = start.latitude();
		var y2 = end.latitude();
		var m = slope(x1,y1, x2, y2);
		var b = y1 - m * x1;
		var dist = helper.euclid(x1, y1, x2, y2);
			for (double i = 0.0; i < dist; i+=0.000001) {
				var lngS = start.longitude()+i;
				var lngE = end.longitude()+i;
				var latS = m * lngS + b;
				var latE = m *lngE + b;
				var startM = Point.fromLngLat(lngS,latS);
				var endM = Point.fromLngLat(lngE,latE);
				for (Polygon poly : polygons) {
			        if (TurfJoins.inside(endM, poly)||TurfJoins.inside(startM, poly)) {
			            intersects = true;
			        }
			    }
			}
		    return intersects;
		}
	
	public double slope(double x1, double y1, double x2, double y2) 
	{ 
	    return (y2 - y1) / (x2 - x1); 
	} 
	
}
