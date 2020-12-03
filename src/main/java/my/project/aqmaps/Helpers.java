package my.project.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

/**
 * Helpers Class description.
 * 
 * This class focuses on providing various helper functions to avoid messy code.
 * It has methods to compute Pythagorean distances, to find the Index in an
 * array, to generate a distance matrix, and to reorder arrays.
 *
 */
public class Helpers {

	/**
	 * This method computes Pythagorean distances. Since for this coursework, we
	 * assume that locations are like points on a plane instead of points on the
	 * surface of a sphere.
	 * 
	 * @param x1 x-coordinate of the first point (double)
	 * @param y1 y-coordinate of the first point (double)
	 * @param x2 x-coordinate of the second point (double)
	 * @param y2 y-coordinate of the second point (double)
	 * @return The Pythagorean distance between the two points (double)
	 */
	public double euclid(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	/**
	 * This method allows us to find the first occurrence of a value in an array
	 * 
	 * @param arr   Array in which we look for the value (array of doubles)
	 * @param value The value we are looking for (double)
	 * @return Index of first occurrence of the value in the array (int)
	 */

	public int findIndex(double arr[], double value) {

		if (arr == null) {
			return -1;
		}

		int len = arr.length;
		int i = 0;
		while (i < len) {

			if (arr[i] == value) {
				return i;
			} else {
				i = i + 1;
			}
		}
		return -1;
	}

	/**
	 * This method allows us to generate a Distance matrix between all points in our
	 * list of Sensors.
	 * 
	 * @param lng    All longitudes of the sensors (double)
	 * @param lat    All latitudes of the sensors (double)
	 * @param length The length of the sensors list (int)
	 * @return A length x length matrix of distances (double) e.g dists[0][2] is the
	 *         distance between the 1st Sensor and the 3rd sensor.
	 */
	public double[][] generateDistanceMatrix(ArrayList<Double> lng, ArrayList<Double> lat, int length) {
		double[][] dists = new double[length][length];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				dists[i][j] = euclid(lng.get(i), lat.get(i), lng.get(j), lat.get(j));
			}
		}
		return dists;
	}

	/**
	 * This method fills arrays so that they contain the sensors in an ordered way.
	 * 
	 * @param route            This parameter gives the required ordering (Integer)
	 * @param sensorsLocation  Original ordering of the sensors Locations (Point)
	 * @param batteries        Original ordering of the battery data (String)
	 * @param readings         Original ordering of the readings data (String)
	 * @param w3wAddress       Original ordering of the what 3 words addresses
	 *                         (String)
	 * @param orderedSensors   New ordering of the sensors (Point)
	 * @param orderedBatteries New ordering of the battery data (String)
	 * @param orderedReadings  New ordering of the readings data (String)
	 * @param w3wOrdered       New ordering of the what 3 words addresses (String)
	 */

	public void reorderArrays(ArrayList<Integer> route, ArrayList<Point> sensorsLocation, ArrayList<String> batteries,
			ArrayList<String> readings, ArrayList<String> w3wAddress, ArrayList<Point> orderedSensors,
			ArrayList<String> orderedBatteries, ArrayList<String> orderedReadings, ArrayList<String> w3wOrdered) {
		for (Integer i : route) {
			orderedSensors.add(sensorsLocation.get(i));
			orderedReadings.add(readings.get(i));
			orderedBatteries.add(batteries.get(i));
			w3wOrdered.add(w3wAddress.get(i));
		}
	}

}
