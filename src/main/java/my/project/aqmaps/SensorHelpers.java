package my.project.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

/**
 * SensorHelpers Class Description
 * This class contains helper functions linked to the sensors. It has methods to:
 * - get all the sensor locations (latitudes and longitudes)
 * - get all the battery readings
 * - get all the longitudes
 * - get all the latitudes
 * - color the feature to be displayed on the GeoJson map
 */
public class SensorHelpers {
	/**
	 * There are two class constructors for that class. One takes the sensorList and
	 * the host as arguments and the other takes no argument. This avoids having to
	 * type new SensorHelpers(null, null) Both will be used in different classes.
	 */
	private ArrayList<Sensor> sensorList;
	private String host;
	final HttpClient client = HttpClient.newHttpClient();

	public SensorHelpers(ArrayList<Sensor> sensorList, String host) {
		this.sensorList = sensorList;
		this.host = host;
	}

	public SensorHelpers() {
		this.sensorList = null;
		this.host = null;
	}

	/**
	 * This method gets all the sensors locations (longitudes and latitude)
	 * 
	 * @param w3wAddress this parameter is the what 3 words address ArrayList. It
	 *                   starts as an empty array but the method adds the
	 *                   corresponding w3w address for each sensor. So it has the
	 *                   same size as the sensors list (String)
	 * @return An ArrayList of sensors locations of type Point (size is the number
	 *         of sensors)
	 * @throws IOException          If there is a problem with the sending request
	 * @throws InterruptedException If there is a problem with the sending request
	 */
	public ArrayList<Point> getSensorLoc(ArrayList<String> w3wAddress) throws IOException, InterruptedException {
		var sensorsLocation = new ArrayList<Point>();
		for (Sensor sensor : sensorList) {
			/* Adding the relevant w3w address to our ArrayList */
			w3wAddress.add(sensor.getLocation());

			/*
			 * We changed the format of the location string to be able to fetch the data
			 * from the server
			 */
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			/*
			 * Like done in the ServerReader class, we use GSon Parsing to get a
			 * LocationDetails object that way we can access the coordinates. The only
			 * difference is that we are deserializing a JSON record instead of a JSON list.
			 * Once we have the coordinates we create a geoJson Point.
			 */
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);
			var loc = Point.fromLngLat(details.coordinates.lng, details.coordinates.lat);

			sensorsLocation.add(loc);
		}
		return sensorsLocation;
	}

	/**
	 * This method gets all the battery readings from the sensors
	 * 
	 * @return An ArrayList of type String storing all the battery data (Size = No.
	 *         of sensors)
	 */
	public ArrayList<String> getBatteries() {
		var batteries = new ArrayList<String>();
		for (Sensor sensor : sensorList) {
			batteries.add(sensor.getBattery());
		}
		return batteries;
	}

	/**
	 * This method gets all the readings data from the sensors
	 * 
	 * @return An ArrayList of type String storing all the readings data (Size = No.
	 *         of sensors)
	 */
	public ArrayList<String> getReadings() {
		var readings = new ArrayList<String>();
		for (Sensor sensor : sensorList) {
			readings.add(sensor.getReading());
		}
		return readings;
	}

	/**
	 * This method gets all the longitudes of the sensors. This will simplify
	 * calculations done in other classes.
	 * 
	 * @return An ArrayList of type Double storing all the longitudes (Size = No. of
	 *         sensors)
	 * @throws IOException          If there is a problem with the sending request
	 * @throws InterruptedException If there is a problem with the sending request
	 */
	public ArrayList<Double> getLongitudes() throws IOException, InterruptedException {
		var lng = new ArrayList<Double>();
		for (Sensor sensor : sensorList) {
			/*
			 * We changed the format of the location string to be able to fetch the data
			 * from the server
			 */
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			/*
			 * Like done in the ServerReader class, we use GSon Parsing to get a
			 * LocationDetails object that way we can access the coordinates. The only
			 * difference is that we are deserializing a JSON record instead of a JSON list.
			 * Once we have the coordinates we create a geoJson Point.
			 */
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);

			lng.add(details.coordinates.lng);
		}
		return lng;
	}

	/**
	 * This method gets all the latitudes of the sensors. This will simplify
	 * calculations done in other classes.
	 * 
	 * @return An ArrayList of type Double storing all the latitudes (Size = No. of
	 *         sensors)
	 * @throws IOException          If there is a problem with the sending request
	 * @throws InterruptedException If there is a problem with the sending request
	 */
	public ArrayList<Double> getLatitudes() throws IOException, InterruptedException {
		var lat = new ArrayList<Double>();
		for (Sensor sensor : sensorList) {
			/*
			 * We changed the format of the location string to be able to fetch the data
			 * from the server
			 */
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			/*
			 * Like done in the ServerReader class, we use GSon Parsing to get a
			 * LocationDetails object that way we can access the coordinates. The only
			 * difference is that we are deserializing a JSON record instead of a JSON list.
			 * Once we have the coordinates we create a geoJson Point.
			 */
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);

			lat.add(details.coordinates.lat);
		}
		return lat;
	}

	/**
	 * This method allows to color the feature that is going to be plotted on the
	 * map given the table provided in the coursework document.
	 * 
	 * @param airQ    The air-quality reading (String)
	 * @param feature The feature that needs to be colored (GeoJson Feature)
	 * @param battery The battery reading (String)
	 * @param address The what 3 words address (String)
	 * @return A colored feature (Geojson feature)
	 */
	public Feature Color(String airQ, Feature feature, String battery, String address) {
		/*
		 * We first need to check if the Air Quality reading is set to null or Nan. If
		 * it is, see case 1 if not see case 2. We also check for the battery reading.
		 * If it is less than or equal to 10 we go to case 2.
		 */
		if (!airQ.equals("null") && !airQ.equals("NaN") && Double.parseDouble(battery) > 10) {
			/*
			 * Case 1: The airQuality reading is not null or NaN. We can convert our String
			 * to a Double. Given the table provided in the coursework, we add different
			 * String properties.
			 */
			double airQuality = Double.parseDouble(airQ);
			if (0 <= airQuality && airQuality < 32) {
				feature.addStringProperty("marker-color", "#00ff00");
				feature.addStringProperty("rgb-string", "#00ff00");
				feature.addStringProperty("marker-symbol", "lighthouse");
				feature.addStringProperty("location", address);
			} else if (32 <= airQuality && airQuality < 64) {
				feature.addStringProperty("marker-color", "#40ff00");
				feature.addStringProperty("rgb-string", "#40ff00");
				feature.addStringProperty("marker-symbol", "lighthouse");
				feature.addStringProperty("location", address);
			} else if (64 <= airQuality && airQuality < 96) {
				feature.addStringProperty("marker-color", "#80ff00");
				feature.addStringProperty("rgb-string", "#80ff00");
				feature.addStringProperty("marker-symbol", "lighthouse");
				feature.addStringProperty("location", address);
			} else if (96 <= airQuality && airQuality < 128) {
				feature.addStringProperty("marker-color", "#c0ff00");
				feature.addStringProperty("rgb-string", "#c0ff00");
				feature.addStringProperty("marker-symbol", "lighthouse");
				feature.addStringProperty("location", address);
			} else if (128 <= airQuality && airQuality < 160) {
				feature.addStringProperty("marker-color", "#ffc000");
				feature.addStringProperty("rgb-string", "#ffc000");
				feature.addStringProperty("marker-symbol", "danger");
				feature.addStringProperty("location", address);
			} else if (160 <= airQuality && airQuality < 192) {
				feature.addStringProperty("marker-color", "#ff8000");
				feature.addStringProperty("rgb-string", "#ff8000");
				feature.addStringProperty("marker-symbol", "danger");
				feature.addStringProperty("location", address);
			} else if (192 <= airQuality && airQuality < 224) {
				feature.addStringProperty("marker-color", "#ff4000");
				feature.addStringProperty("rgb-string", "#ff4000");
				feature.addStringProperty("marker-symbol", "danger");
				feature.addStringProperty("location", address);
			} else if (224 <= airQuality && airQuality < 256) {
				feature.addStringProperty("marker-color", "#ff0000");
				feature.addStringProperty("rgb-string", "#ff0000");
				feature.addStringProperty("marker-symbol", "danger");
				feature.addStringProperty("location", address);
			} else {
				/*
				 * If the number fetched is not included in the previous cases we throw an
				 * illegal argument exception.
				 */
				throw new IllegalArgumentException("Value out of bound (" + airQuality + ")");
			}
		} else if (airQ.equals("null") || airQ.equals("NaN") || Double.parseDouble(battery) <= 10) {
			/*
			 * Case 2: Air Quality is null or Nan OR battery reading is less than or equal
			 * to 10. The feature is black and has a white cross on it to indicate a faulty
			 * sensor.
			 */
			feature.addStringProperty("marker-color", "#000000");
			feature.addStringProperty("rgb-string", "#000000");
			feature.addStringProperty("marker-symbol", "cross");
			feature.addStringProperty("location", address);
		}
		return feature;
	}
}
