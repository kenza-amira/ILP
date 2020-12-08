package my.project.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

/**
 * GraphSearch Class Description
 * 
 * This class contains all the methods related to graph search. I've decided to
 * implement a Two-Opt heuristic. There will be all necessary methods needed to
 * run the heuristic in that class.
 *
 */
public class GraphSearch implements Helpers{
	private ArrayList<Point> sensorsLocation;
	private Point start;
	ArrayList<Integer> route = new ArrayList<>();

	/**
	 * This is the class constructor it takes two arguments.
	 * 
	 * @param sensorsLocation ArrayList of original (unordered) sensors locations
	 *                        (GeoJson Point)
	 * @param start           This is the starting point of the drone.
	 */

	public GraphSearch(ArrayList<Point> sensorsLocation, Point start) {
		this.sensorsLocation = sensorsLocation;
		this.start = start;
	}

	/**
	 * This method implements the TwoOpt heuristic.
	 * 
	 * @param dists This is the No. of sensors x No. of sensors distance matrix that
	 *              contains the distances between every point to every other point
	 * @return This method returns an ArrayList of Integer that contains our route
	 *         (indexes of Points). This will be used to sort all the Sensors.
	 */
	public ArrayList<Integer> twoOpt(double[][] dists) {

		/*
		 * We start by initializing route and newRoute, they will contain the integers
		 * between 0 and No.of sensors. These integers represent the indexes of our
		 * sensorsLocation points
		 */
		var newTour = new ArrayList<Integer>();

		for (int i = 0; i < sensorsLocation.size(); i++) {
			route.add(i);
			newTour.add(i);
		}
		int size = route.size();

		// Out while loop while continue looping until no more improvement is made
		int better = 0;
		while (better < 1000) {
			double best_distance = tourValue(route, dists);

			for (int i = 1; i < size - 1; i++) {
				for (int k = i + 1; k < size; k++) {
					// We try the swap and compute the new distance
					TwoOptSwap(i, k, route, newTour);
					double new_distance = tourValue(newTour, dists);

					if (new_distance < best_distance) {
						// If an improvement is made, the improve counter resets.
						better = 0;

						for (int j = 0; j < size; j++) {
							route.set(j, newTour.get(j));
						}

						best_distance = new_distance;

					}
				}
			}

			better++;
		}
		return route;
	}

	/**
	 * This method is a helper method for the Two-Opt heuristic, it takes care of
	 * computing the total distance of an entire tour, from Starting point to
	 * Starting point while visiting all the sensors.
	 * 
	 * @param newRoute This is the route we want to know the tour Value of
	 *                 (ArrayList of Integer)
	 * @param dists    This is our distance matrix from every point to every other
	 *                 point (Double)
	 * @return This method returns the total distance (Double)
	 */

	public double tourValue(ArrayList<Integer> newRoute, double[][] dists) {
		double value = 0;
		int first = newRoute.get(0);
		int end = newRoute.get(newRoute.size() - 1);
		value += euclid(start.longitude(), start.latitude(), sensorsLocation.get(first).longitude(),
				sensorsLocation.get(first).latitude());
		for (int i = 0; i < newRoute.size() - 1; i++) {
			value += dists[newRoute.get(i)][newRoute.get(i + 1)];
		}
		value += euclid(start.longitude(), start.latitude(), sensorsLocation.get(end).longitude(),
				sensorsLocation.get(end).latitude());
		return value;
	}

	/**
	 * This method is our second helper function. It takes care of swapping the
	 * elements since we need to reverse a slice of the arrayList and leave the rest
	 * as it is
	 * 
	 * @param i        This is the start of the slice (int)
	 * @param k        This is the end of the slice (int)
	 * @param route    This is the original ArrayList of Integer representing our
	 *                 route (ArrayList of Integer)
	 * @param newRoute This is the modified ArrayList of Integer representing our
	 *                 new route (ArrayList of Integer)
	 */
	void TwoOptSwap(int i, int k, ArrayList<Integer> route, ArrayList<Integer> newRoute) {
		int size = route.size();

		// 1. take route[0] to route[i-1] and add them in order to newRoute
		for (int p = 0; p <= i - 1; p++) {
			newRoute.set(p, route.get(p));
		}
		// 2. take route[i] to route[k] and add them in reverse order to newRoute
		int reverse = 0;
		for (int p = i; p <= k; p++) {
			newRoute.set(p, route.get(k - reverse));
			reverse++;
		}
		// 3. take route[k+1] to end and add them in order to newRoute
		for (int p = k + 1; p < size; p++) {
			newRoute.set(p, route.get(p));
		}
	}
}
