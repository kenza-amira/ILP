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

public class SensorHelpers {
	public ArrayList<Point> getSensorLoc (ArrayList<Sensor> sensorList, String host, HttpClient client, Point start) throws IOException, InterruptedException{
		var sensorsLocation = new ArrayList<Point>();
		sensorsLocation.add(start);
		for (Sensor sensor : sensorList) {
			// We changed the format of the location string to be able to fetch the data
			// from the server
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			// Like before, we use GSon Parsing to get a LocationDetails object that way we
			// can acess
			// the coordinates. The only difference is that we are deserializing a JSON
			// record instead
			// of a JSON list. Once we have the coordinates we create a geoJson Point.
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);
			var loc = Point.fromLngLat(details.coordinates.lng, details.coordinates.lat);
			sensorsLocation.add(loc);
		}
		return sensorsLocation;
	}
	
	public ArrayList<String> getBatteries(ArrayList<Sensor> sensorList){
		var batteries = new ArrayList<String>();
		batteries.add("0");
		for (Sensor sensor : sensorList) {
			batteries.add(sensor.getBattery());
		}
		return batteries;
	}
	
	public ArrayList<String> getReadings(ArrayList<Sensor> sensorList){
		var readings = new ArrayList<String>();
		readings.add("NaN");
		for (Sensor sensor : sensorList) {
			readings.add(sensor.getReading());
		}
		return readings;
	}
	
	public ArrayList<Double> getLongitudes(ArrayList<Sensor> sensorList, Point start, String host, HttpClient client) throws IOException, InterruptedException{
		var lng = new ArrayList<Double>();
		lng.add(start.longitude());
		for (Sensor sensor : sensorList) {
			// We changed the format of the location string to be able to fetch the data
			// from the server
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			// Like before, we use GSon Parsing to get a LocationDetails object that way we
			// can acess
			// the coordinates. The only difference is that we are deserializing a JSON
			// record instead
			// of a JSON list.
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);
			lng.add(details.coordinates.lng);
		}
		return lng;
	}
	
	public ArrayList<Double> getLatitudes(ArrayList<Sensor> sensorList, Point start, String host, HttpClient client) throws IOException, InterruptedException{
		var lat = new ArrayList<Double>();
		lat.add(start.latitude());
		for (Sensor sensor : sensorList) {
			// We changed the format of the location string to be able to fetch the data
			// from the server
			String location = sensor.getLocation().trim().replace(".", "/");
			String urlLoc = host + "/words/" + location + "/details.json";
			var requestL = HttpRequest.newBuilder().uri(URI.create(urlLoc)).build();
			var responseL = client.send(requestL, BodyHandlers.ofString());

			// Like before, we use GSon Parsing to get a LocationDetails object that way we
			// can acess
			// the coordinates. The only difference is that we are deserializing a JSON
			// record instead
			// of a JSON list.
			var details = new Gson().fromJson(responseL.body(), LocationDetails.class);
			lat.add(details.coordinates.lat);
		}
		return lat;
	}
	
	public Feature Color(String airQ, Feature feature, String battery) {
		if (!airQ.equals("null") && !airQ.equals("NaN") && 
				Double.parseDouble(battery) > 10) {
			double airQuality = Double.parseDouble(airQ);
				if (0 <= airQuality && airQuality < 32) {
					feature.addStringProperty("marker-color", "#00ff00");
					feature.addStringProperty("rgb-string", "#00ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (32 <= airQuality && airQuality < 64) {
					feature.addStringProperty("marker-color", "#40ff00");
					feature.addStringProperty("rgb-string", "#40ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (64 <= airQuality && airQuality < 96) {
					feature.addStringProperty("marker-color", "#80ff00");
					feature.addStringProperty("rgb-string", "#80ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (96 <= airQuality && airQuality < 128) {
					feature.addStringProperty("marker-color", "#c0ff00");
					feature.addStringProperty("rgb-string", "#c0ff00");
					feature.addStringProperty("marker-symbol", "lighthouse");
				} else if (128 <= airQuality && airQuality < 160) {
					feature.addStringProperty("marker-color", "#ffc000");
					feature.addStringProperty("rgb-string", "#ffc000");
					feature.addStringProperty("marker-symbol", "danger");
				} else if (160 <= airQuality && airQuality < 192) {
					feature.addStringProperty("marker-color", "#ff8000");
					feature.addStringProperty("rgb-string", "#ff8000");
					feature.addStringProperty("marker-symbol", "danger");
				} else if (192 <= airQuality && airQuality < 224) {
					feature.addStringProperty("marker-color", "#ff4000");
					feature.addStringProperty("rgb-string", "#ff4000");
					feature.addStringProperty("marker-symbol", "danger");
				} else if (224 <= airQuality && airQuality < 256) {
					feature.addStringProperty("marker-color", "#ff0000");
					feature.addStringProperty("rgb-string", "#ff0000");
					feature.addStringProperty("marker-symbol", "danger");
				} else {
					// If the number fetched is not included int the previous cases we throw
					// an illegal argument excpetion.
					throw new IllegalArgumentException("Value out of bound (" + airQuality + ")");
				}
		} else if(airQ.equals("null") || airQ.equals("NaN") || 
				Double.parseDouble(battery) <= 10){
			feature.addStringProperty("marker-color", "#000000");
			feature.addStringProperty("rgb-string", "#000000");
			feature.addStringProperty("marker-symbol", "cross");
		}
		return feature;
	}

}
