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

public class ServerReader {
	private String host;
	private String month;
	private String day;
	private String year;
	final HttpClient client = HttpClient.newHttpClient();
	
	public ServerReader(String host, String month, String day, String year) {
		this.host = host;
		this.month = month;
		this.day = day;
		this.year = year;
	}
	
	/**
	 * This part of the code fetches the information stored in the web server maps
	 * folder. It uses the first 3 arguments (day, month, year) to get the sensors
	 * that need to be visited on that date. It then uses Gson parsing to to
	 * deserialise the JSON list into an object of type ArrayList<Sensor>. Since We
	 * can’t get a class associated with ArrayList<Sensor> we get its Type using
	 * Java’s Reflection API which is used to examine or modify methods, classes, or
	 * interfaces at runtime. This allows use to have a Sensor Object (With
	 * location, battery and reading) and to have a List of sensors to visit on that
	 * date.
	 * 
	 */
	
	public ArrayList<Sensor> getSensorList() throws IOException, InterruptedException{
		String urlVisit = host + "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";

		var requestV = HttpRequest.newBuilder().uri(URI.create(urlVisit)).build();
		var responseV = client.send(requestV, BodyHandlers.ofString());

		Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
		ArrayList<Sensor> sensorList = new Gson().fromJson(responseV.body(), listType);
		
		return sensorList;
	}
	
	/**
	 * Here we use GeoJson parsing to retrieve data about the no fly zones. Like
	 * before, we access information stored on the web server only this time it's in
	 * the buildings folder. Since it is a geoJSON string. We are able to easily
	 * retrieve the Feature Collection of our no fly zones. I've also created an
	 * ArrayList of LineString to store the LineStrings that make our Polygon. This 
	 * will be helpful later on for the path finding
	 */
	
	public ArrayList<LineString> getNoFlyZones() throws IOException, InterruptedException{
		String urlDeadZone = host + "/buildings/no-fly-zones.geojson";
		var requestD = HttpRequest.newBuilder().uri(URI.create(urlDeadZone)).build();
		var responseD = client.send(requestD, BodyHandlers.ofString());
		var obstacles = FeatureCollection.fromJson(responseD.body());
		var noFly = obstacles.features();
		
		var allZones = new ArrayList<LineString>();
		for (Feature f: noFly) {
			var geo = f.geometry();
			var poly = (Polygon)geo;
			allZones.add(poly.outer());
		}
		return allZones;
	}

	
}
