package my.project.aqmaps;

/**
 * This class was created for Parsing purposes. It allows us to have Sensor
 * Objects after accessing the web server.
 *
 */

public class Sensor {
	private String location;
	private String battery;
	private String reading;

	public Sensor(String loc, String bat, String read) {
		this.location = loc;
		this.battery = bat;
		this.reading = read;
	}

	// Getter Functions
	public String getLocation() {
		return location;
	}

	public String getBattery() {
		return battery;
	}

	public String getReading() {
		return reading;
	}

}
