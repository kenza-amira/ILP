package my.project.aqmaps;

import java.util.ArrayList;

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

}
