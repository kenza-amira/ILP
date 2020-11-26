package my.project.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException
    {
    	// We only need one HttpClient, shared between all HttpRequests
    	final HttpClient client = HttpClient.newHttpClient();
    	// Saving this string as it will be needed for multiple URL's.
    	final String host = "http://localhost:"+args[6];
    	// Initializing features list that we will need for our geojson output.
    	var features = new ArrayList<Feature>();
    	// Using the 6th argument as the random number seed for the application.
    	try {
    	    int seed = Integer.parseInt(args[5]);
    	}catch (NumberFormatException nfe) {
    	    throw new IllegalArgumentException("The seed entered, " + args[5]+ " is invalid.", nfe);
    	}
    	
    	/** 
    	 * This part of the code fetches the information stored in the web server maps folder.
    	 * It uses the first 3 arguments (day, month, year) to get the sensors that need to be
    	 * visited on that date. It then uses Gson parsing to to deserialise the JSON list into 
    	 * an object of type ArrayList<Sensor>. Since We can’t get a class associated 
    	 * with ArrayList<Sensor> we get its Type using Java’s Reflection API which is used 
    	 * to examine or modify methods, classes, or interfaces at runtime. This allows use to
    	 * have a Sensor Object (With location, battery and reading) and to have a List of sensors
    	 * to visit on that date.
    	 * 
    	 */
        String urlVisit = host+"/maps/"+args[2]+"/"
        		+ args[1]+"/"+args[0]+"/air-quality-data.json";
        
        var requestV = HttpRequest.newBuilder()
        		.uri(URI.create(urlVisit))
        		.build();
        var responseV = client.send(requestV, BodyHandlers.ofString());
        
        Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
        ArrayList<Sensor> sensorList = new Gson().fromJson(responseV.body(), listType);
        
        /**
         * Here we use GeoJson parsing to retrieve data about the no fly zones. Like before, we access
         * information stored on the web server only this time it's in the buildings folder. Since it is
         * a geoJSON string. We are able to easily retrieve the Feature Collection of our no fly zones.
         */
        String urlDeadZone = host+"/buildings/no-fly-zones.geojson";
        var requestD = HttpRequest.newBuilder()
        		.uri(URI.create(urlDeadZone))
        		.build();
        var responseD = client.send(requestD, BodyHandlers.ofString());
        var fc = FeatureCollection.fromJson(responseD.body());
        var noFly = fc.features();
        //System.out.println(responseV.body());
        //System.out.println(responseD.body());
       
        /**
         * We have previously formed an array of Sensor objects. Since we now have a Sensor class
         * we are able to retrieve specific details about our sensors. In this step, we are interested
         * in the location. We get the location which is a What3Words address. But in order to create
         * points for our map we need the longitude and latitude associated with this address. We access
         * the words folder in our web server to find this information and create a list of points for our map.
         */
        var sensorsLocation = new ArrayList<Point>();
        for (Sensor sensor: sensorList) {
        	//We changed the format of the location string to be able to fetch the data from the server
        	String location = sensor.getLocation().trim().replace(".", "/");
        	String urlLoc = host +"/words/"+location+"/details.json";
        	var requestL = HttpRequest.newBuilder()
            		.uri(URI.create(urlLoc))
            		.build();
        	var responseL = client.send(requestL, BodyHandlers.ofString());
        	
        	//Like before, we use GSon Parsing to get a LocationDetails object that way we can acess
        	//the coordinates. The only difference is that we are deserializing a JSON record instead
        	//of a JSON list. Once we have the coordinates we create a geoJson Point.
        	var details = new Gson().fromJson(responseL.body(), LocationDetails.class);
        	var loc = Point.fromLngLat(details.coordinates.lng, details.coordinates.lat);
        	sensorsLocation.add(loc);       	
        }

        for (Point p : sensorsLocation) {
        	var feature = Feature.fromGeometry((Geometry)p);
        	features.add(feature);
        }
        features.addAll(noFly);
        var collections = FeatureCollection.fromFeatures(features);
        System.out.println(collections.toJson());
        
        
    }
}
