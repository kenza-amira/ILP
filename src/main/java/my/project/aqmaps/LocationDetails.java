package my.project.aqmaps;

/**
 * This class was created for Parsing. It stores the details for the Sensors.
 * What we are the most interested in, are the coordinates (longitude and
 * latitude).
 *
 */
public class LocationDetails {
	String country;
	String words;
	String language;
	String map;
	String nearestPlace;

	Coordinates coordinates;

	public static class Coordinates {
		double lng;
		double lat;

		public Coordinates(double lng, double lat) {
			this.lng = lng;
			this.lat = lat;
		}

		// Getter functions
		public double getLng() {
			return lng;
		}

		public double getLat() {
			return lat;
		}
	}

	Square square;

	public static class Square {
		Southwest southwest;
		Northeast northeast;

		public static class Southwest {
			double lng;
			double lat;

			public Southwest(double lng, double lat) {
				this.lng = lng;
				this.lat = lat;
			}
		}

		public static class Northeast {
			double lng;
			double lat;

			public Northeast(double lng, double lat) {
				this.lng = lng;
				this.lat = lat;
			}
		}

		public Square(Southwest southwest, Northeast northeast) {
			this.southwest = southwest;
			this.northeast = northeast;
		}
	}

	public LocationDetails(String country, String words, String language, String map, String nearestPlace,
			Coordinates coordinates, Square square) {
		this.country = country;
		this.words = words;
		this.language = language;
		this.map = map;
		this.nearestPlace = nearestPlace;
		this.coordinates = coordinates;
		this.square = square;
	}

	// Getter functions
	public String getCountry() {
		return country;
	}

	public String getWords() {
		return words;
	}

	public String getLanguage() {
		return language;
	}

	public String getMap() {
		return map;
	}

	public String getNearestPlace() {
		return nearestPlace;
	}

	public Coordinates getCoord() {
		return coordinates;
	}

	public Square getSquare() {
		return square;
	}

}
