package my.project.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;

public class Helpers {
	
	public double euclid(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
	
	public int findIndex(double arr[], double value) {

		// If array is Null
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
	
	public int findIndex(ArrayList<Double> arr, double value) {

		// If array is Null
		if (arr == null) {
			return -1;
		}

		// find length of array
		int len = arr.size();
		int i = 0;

		// traverse in the array
		while (i < len) {

			// if the i-th element is t
			// then return the index
			if (arr.get(i) == value) {
				return i;
			} else {
				i = i + 1;
			}
		}
		return -1;
	}
	
	public double[][] generateDistanceMatrix(ArrayList<Double> lng, ArrayList<Double> lat, int length){
		double[][] dists = new double[length][length];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				dists[i][j] = euclid(lng.get(i), lat.get(i), lng.get(j), lat.get(j));
			}
		}
		return dists;
	}
	
	public void reorderArrays (ArrayList<Integer> route, ArrayList<Point> sensorsLocation,
ArrayList<String> batteries, ArrayList<String> readings, ArrayList<String> w3wAddress,
ArrayList<Point> orderedSensors, ArrayList<String> orderedBatteries,
ArrayList<String> orderedReadings, ArrayList<String>w3wOrdered) {
		for (Integer i : route) {
			orderedSensors.add(sensorsLocation.get(i));
			orderedReadings.add(readings.get(i));
			orderedBatteries.add(batteries.get(i));
			w3wOrdered.add(w3wAddress.get(i));
		}
	}

}
