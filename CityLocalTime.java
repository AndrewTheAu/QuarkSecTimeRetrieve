import java.util.*;
import java.util.regex.*;
import java.io.File;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class CityLocalTime {
	private static final int ATTRIBUTES = 4;
	private static final int LOCATION = 0;
	private static final int TIMEZONE = 1;
	private static final int DATE = 2;
	private static final int TIME = 3;


	private static final int TIMEZONE_PADDING = 25;
	private static final int LOCATION_PADDING = 50;
	
	private static final int HOUR_PADDING = 2;
	private static final int MINUTES_PADDING = 2;
	private static final int SECONDS_PADDING = 2;

	private static final int DAY_PADDING = 11;
	private static final int MONTH_PADDING = 9;
	private static final int DATE_PADDING = 2;
	private static final int YEAR_PADDING = 4;

	private static final int DELIMIT_PADDING = 5;

	public static void main(String args[]) {
		Scanner userInput = new Scanner(System.in);

		do {
			HashMap<String, Boolean> properties = applicationProperties();
			String[] cities = requestCities(userInput);
			String[][] output = new String[cities.length][ATTRIBUTES];

			// BUILD DATE FORMATTER
			DateTimeFormatter dateFormatter = buildDateFormat(properties);
			// BUILD TIME FORMATTER
			DateTimeFormatter timeFormatter = buildTimeFormat(properties);

			System.out.println();
			int idx = 0;
			for (String city : cities) {
				String timeZoneID = getTimeZone(city);
				DateTime cityDateTime = DateTime.now(DateTimeZone.forID(timeZoneID));
				city = getFullIdentity(city);
				// DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("EEEE, MMMM dd, yyyy");
				DateTimeFormatter time12Formatter = DateTimeFormat.forPattern("hh:mm:ss a");
				DateTimeFormatter time24Formatter = DateTimeFormat.forPattern("kk:mm:ss");
				System.out.println(city + " | " + timeZoneID + " | " + dateFormatter.print(cityDateTime) + " | " + time12Formatter.print(cityDateTime) + " | " + time24Formatter.print(cityDateTime));
				output[idx][LOCATION] = city;
				output[idx][TIMEZONE] = timeZoneID;
				output[idx][DATE] = dateFormatter.print(cityDateTime);
				output[idx++][TIME] = timeFormatter.print(cityDateTime);
				// FUNCTION PRINT THE TIME OF THE CITY
			}
			System.out.println();


			if (properties.get("sort")) {
				Arrays.sort(output, new java.util.Comparator<String[]>() {
					public int compare(String[] a, String[] b) {
						return a[0].compareTo(b[0]);
					}
				});
			}
			for (String[] city : output) {
				System.out.print("| ");
				if (properties.get("location")) System.out.print(city[LOCATION] + " | ");
				if (properties.get("timezone")) System.out.print(city[TIMEZONE] + " | ");
				if (properties.get("date")) System.out.print(city[DATE] + " | ");
				if (properties.get("time")) System.out.print(city[TIME] + " | ");
				System.out.println();
			}
		} while (requestRepeatProgram(userInput));
		userInput.close();
	}

	public static HashMap<String, Boolean> applicationProperties() {
		HashMap<String, Boolean> properties = new HashMap<String, Boolean>();
		try {
			File propertiesFile = new File("./properties.txt");
			Scanner propertyLine = new Scanner(propertiesFile);
			String property;

			while (propertyLine.hasNextLine()) {
				property = propertyLine.nextLine();

				if (!property.isEmpty() && !property.contains("#")) {
					// System.out.println(property);
					properties.put(
							property.substring(0, property.indexOf(",")), 
							property.substring(property.indexOf(",") + 1).equalsIgnoreCase("TRUE") ?
									true : false
					);
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// System.out.println(properties);
		return properties;
	}

	static private String[] requestCities (Scanner userInput) {
		String[] cities;

		System.out.println(
				"\nEnter the cities that you would like the time of, " +
				"separate each city by a '|'.\n" +
				"If the incorrect city is displayed, try the following format: " +
				"CITY, STATE/PROVINCE, COUNTRY."
		);
		cities = userInput.nextLine().split("\\|");

		String[] cleaned_cities = new String[cities.length];
		int idx = 0;
		for (String city : cities) {
			cleaned_cities[idx++] = city.trim();
		}

		return cleaned_cities;
	}

	static private String getFullIdentity(String city) {
		try {
			Document doc = Jsoup.connect("http://www.time.is/" + city).get();

			return doc.select("#msgdiv").select("h1").text().replace("Time in ", "").replace(" now", "");
		} catch (Exception e) {
			// System.err.println(e.getMessage());
			return city + " was not found";
		}
	}

	static private String getTimeZone(String city) {
		try {
			Document doc = Jsoup.connect("http://www.time.is/" + city).get();
			String timeZoneID = doc.select(".infobox").text();
			Pattern timeZonePattern = Pattern.compile("[\\w]+/[\\w]+(?=\\.)");
			Matcher timeZoneMatch = timeZonePattern.matcher(timeZoneID);
			timeZoneMatch.find();
			
			return timeZoneMatch.group(0);
		} catch (Exception e) {
			// System.err.println(e.getMessage());
			return "Not Found";
		}
	}

	static private DateTimeFormatter buildDateFormat (HashMap<String, Boolean> properties) {
		String dateFormat = "";

		if (properties.get("weekday")) {
			dateFormat += properties.get("full_weekday") ? "EEEE" : "EE";
			dateFormat += ", ";
		}
		dateFormat += properties.get("full_month") ? "MMMM " : "MM/";
		dateFormat += properties.get("full_month") ? "dd, " : "dd/";
		dateFormat += properties.get("full_year") ? "yyyy" : "yy";

		return DateTimeFormat.forPattern(dateFormat);
	}

	static private DateTimeFormatter buildTimeFormat (HashMap<String, Boolean> properties) {
		String timeFormat = "";

		timeFormat += properties.get("24hour") ? "kk:mm" : "hh:mm";
		timeFormat += properties.get("seconds") ? ":ss" : "";
		timeFormat += properties.get("24hour") ? "" : " a";

		return DateTimeFormat.forPattern(timeFormat);
	}

	static private boolean requestRepeatProgram(Scanner userInput) {
		System.out.print("Would you like to enter more cities (Y/n): ");
		while (true) {
			String repeatFlag = userInput.nextLine();
			switch (repeatFlag.toUpperCase()) {
				case "Y":
					return true;
				case "N":
					System.out.println("Application shutting down");
					return false;
				default:
					System.out.print("Please enter Y or n: ");
			}
		}
	}

}
