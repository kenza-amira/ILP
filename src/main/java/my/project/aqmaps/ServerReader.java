package my.project.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Polygon;

/**
 * Server Reader Class description.
 * 
 * This class focuses on reading data from the web server. It contains the
 * methods to get a list of sensors and a method to retried no-fly zones. The
 * ServerReader object takes as arguments: String host: contains the port we
 * want to connect to String month, day and year: to access the relevant folder
 *
 */

public class ServerReader {
	private String host;
	private String month;
	private String day;
	private String year;

	/* We only need one HttpClient for all requests */
	final HttpClient client = HttpClient.newHttpClient();

	public ServerReader(String host, String month, String day, String year) {
		this.host = host;
		this.month = month;
		this.day = day;
		this.year = year;
	}

	/**
	 * This part of the code fetches the information stored in the web server maps
	 * folder. It uses the 3 following arguments: day, month and year to get the
	 * sensors that need to be visited on that date. It then uses Gson parsing to to
	 * deserialise the JSON list into an object of type ArrayList of Sensor.
	 * <p>
	 * Since we can’t get a class associated with ArrayList of Sensors we get its
	 * Type using Java’s Reflection API which is used to examine or modify methods,
	 * classes, or interfaces at runtime. This allows use to have a Sensor Object
	 * (With location, battery and reading).
	 * 
	 * @throws IOException if there's a problem with sending the request
	 * @throws InterruptedException if there's a problem with sending the request
	 * @return This method returns the list of sensors to visit on that date
	 */

	public ArrayList<Sensor> getSensorList() throws IOException, InterruptedException {
		String urlVisit = host + "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";

		var requestVisit = HttpRequest.newBuilder().uri(URI.create(urlVisit)).build();
		var responseVisit = client.send(requestVisit, BodyHandlers.ofString());

		Type listType = new TypeToken<ArrayList<Sensor>>() {
		}.getType();
		ArrayList<Sensor> sensorList = new Gson().fromJson(responseVisit.body(), listType);

		return sensorList;
	}

	/**
	 * Here we use GeoJson parsing to retrieve data about the no fly zones. Like
	 * before, we access information stored on the web server only this time it's in
	 * the buildings folder. Since it is a geoJSON string. We are able to easily
	 * retrieve the Feature Collection of our no fly zones.
	 * 
	 * @throws IOException if there's a problem with sending the request
	 * @throws InterruptedException if there's a problem with sending the request
	 * @return This method returns an ArrayList of type LineString that contains the
	 *         outer parts of our polygons.
	 */

	public ArrayList<LineString> getNoFlyZones() throws IOException, InterruptedException {
		String urlDeadZone = host + "/buildings/no-fly-zones.geojson";

		var requestDeadZone = HttpRequest.newBuilder().uri(URI.create(urlDeadZone)).build();
		var responseDeadZone = client.send(requestDeadZone, BodyHandlers.ofString());
		var obstacles = FeatureCollection.fromJson(responseDeadZone.body());

		var noFly = obstacles.features();

		var allZones = new ArrayList<LineString>();
		for (Feature f : noFly) {
			var geo = f.geometry();
			var polygone = (Polygon) geo;
			allZones.add(polygone.outer());
		}
		return allZones;
	}

}
