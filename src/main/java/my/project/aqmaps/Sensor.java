package my.project.aqmaps;

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
