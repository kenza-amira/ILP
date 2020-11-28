package my.project.aqmaps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.mapbox.geojson.Point;

public class GraphSearch {
	
	public ArrayList<Point> greedySearch (double[][] dists, int length, ArrayList<Point> sensorsLocation) {
		var visited = new ArrayList<Integer>();
		var route = new ArrayList<Integer>();
		int counter = 1;
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(0);
		route.add(0);
		var helper = new Helpers();
		//System.out.println(dists);
		while (!queue.isEmpty() && counter < length-1) {
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
			System.out.println(index);
			queue.add(index);
			visited.add(index);
			route.add(index);
		}
		//System.out.println(route.size());
		var orderedSensors = new ArrayList<Point>();
		for (Integer i : route) {
			orderedSensors.add(sensorsLocation.get(i));
		}
		
		return orderedSensors;
	}
}
