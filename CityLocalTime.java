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
	// Consider making this into ENUM
	private static final int ATTRIBUTES = 4;
	private static final int LOCATION = 0;
	private static final int TIMEZONE = 1;
	private static final int DATE = 2;
	private static final int TIME = 3;

	private static final int CITY = 0;
	private static final int REGION = 1;
	private static final int COUNTRY = 2;	

	public static void main(String args[]) {
		Scanner userInput = new Scanner(System.in);

		do {
			// Properties of what the application prints
			HashMap<String, Boolean> properties = applicationProperties();
			// Cities to get information about
			String[] cities = requestCities(userInput);
			String progressBar = "";
			for (String city : cities) {
				progressBar += "-";
			}

			// BUILD DATE FORMATTER
			DateTimeFormatter dateFormatter = buildDateFormat(properties);
			// BUILD TIME FORMATTER
			DateTimeFormatter timeFormatter = buildTimeFormat(properties);
			// Information about each city
			String[][] outputGrid = new String[cities.length][ATTRIBUTES];

			System.out.println();
			int idx = 0;
			for (String city : cities) {
				String timeZoneID = getTimeZone(city);
				DateTime cityDateTime = DateTime.now(DateTimeZone.forID(timeZoneID)); // Move this and rest of for loop into a function
				city = getFullIdentity(city);

				outputGrid[idx][LOCATION] = city; // cityFormatter.print(city);
				outputGrid[idx][TIMEZONE] = timeZoneID;
				outputGrid[idx][DATE] = dateFormatter.print(cityDateTime);
				outputGrid[idx++][TIME] = timeFormatter.print(cityDateTime);
				progressBar = updateProgress(city, progressBar);
			}
			outputGrid = properties.get("pretty") ? prettyFormat(outputGrid) : outputGrid;
			System.out.println();
			System.out.println();

			if (properties.get("sort")) {
				Arrays.sort(outputGrid, new java.util.Comparator<String[]>() {
					public int compare(String[] a, String[] b) {
						return a[0].compareTo(b[0]);
					}
				});
			}
			for (String[] city : outputGrid) {
				System.out.print("| ");
				if(properties.get("location")) System.out.print(city[LOCATION] + " | ");
				if(properties.get("timezone")) System.out.print(city[TIMEZONE] + " | ");
				if(properties.get("date")) System.out.print(city[DATE] + " | ");
				if(properties.get("time")) System.out.print(city[TIME] + " | ");
				System.out.println();
			}

			System.out.println();
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

	static private String[] requestCities(Scanner userInput) {
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
			System.err.println(e.getMessage());
			return "Not Found";
		}
	}

	static private String[][] prettyFormat (String[][] outputGrid) {
		int[] maxLength = new int[ATTRIBUTES];
		int[] padding = new int[ATTRIBUTES];
		int idx = 0;

		for (String[] city : outputGrid) {
			maxLength[LOCATION] = city[LOCATION].length() > maxLength[LOCATION] ? city[LOCATION].length() : maxLength[LOCATION];
			maxLength[TIMEZONE] = city[TIMEZONE].length() > maxLength[TIMEZONE] ? city[TIMEZONE].length() : maxLength[TIMEZONE];
			maxLength[DATE] = city[DATE].length() > maxLength[DATE] ? city[DATE].length() : maxLength[DATE];
			maxLength[TIME] = city[TIME].length() > maxLength[TIME] ? city[TIME].length() : maxLength[TIME];
		}

		for (int pad : maxLength) {
			padding[idx++] = 0 - pad;
		}

		for (idx = 0 ; idx < outputGrid.length ; idx++) {
			outputGrid[idx][LOCATION] = String.format("%" + padding[LOCATION] + "s", outputGrid[idx][LOCATION]);
			outputGrid[idx][TIMEZONE] = String.format("%" + padding[TIMEZONE] + "s", outputGrid[idx][TIMEZONE]);
			outputGrid[idx][DATE] = String.format("%" + padding[DATE] + "s", outputGrid[idx][DATE]);
			outputGrid[idx][TIME] = String.format("%" + padding[TIME] + "s", outputGrid[idx][TIME]);
		}

		return outputGrid;
	}

	static private DateTimeFormatter buildDateFormat(HashMap<String, Boolean> properties) {
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

	static private DateTimeFormatter buildTimeFormat(HashMap<String, Boolean> properties) {
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

	static private String updateProgress(String city, String progressBar) {
		float total = progressBar.length();
		progressBar = progressBar.replaceFirst("-", Character.toString((char)0x2588));

		float current = 0;
		for (char block : progressBar.toCharArray()) {
			if (block == (char)0x2588) current++;
		}

		System.out.print(
				(current/total*100 != 100 ? String.format("Processing: %-50s ", city) : String.format("%-63s", "Process Complete: ")) + 
				progressBar + "\t\t" + 
				current/total*100 + "%\r"
		);

		return progressBar;
	}

}
