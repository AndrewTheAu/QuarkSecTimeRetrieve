import java.util.*;
import java.util.regex.*;
import java.io.*;
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
		// Document retrieved from time.is from cities entered
		Document cityDoc = null;
		// Properties of what the application prints
		Properties properties = applicationProperties();
		// Cities to get information about
		List<String> cities = requestCities(userInput);
		// BUILD DATE FORMATTER
		DateTimeFormatter dateFormatter = buildDateFormat(properties);
		// BUILD TIME FORMATTER
		DateTimeFormatter timeFormatter = buildTimeFormat(properties);
		// Information about each city
		List<List<String>> outputGrid = new ArrayList<List<String>>();
		List<String> unfound = new ArrayList<String>();

		do {
			// Initiate empty progress bar
			String progressBar = "";
			for (String city : cities) {
				progressBar += "-";
			}

			System.out.println();
			List<String> cityProperties = new ArrayList<String>();
			for (String city : cities) {
				cityProperties.clear();
				cityDoc = retreiveCityDoc(city);

				if (cityDoc == null) {
					unfound.add(city);
					progressBar = updateProgress(city, progressBar);
					continue;
				}

				String timeZoneID = getTimeZone(cityDoc);
				DateTime cityDateTime = DateTime.now(DateTimeZone.forID(timeZoneID)); // Move this and rest of for loop into a function
				city = getFullIdentity(cityDoc);

				cityProperties.add(city); // cityFormatter.print(city);
				cityProperties.add(timeZoneID);
				cityProperties.add(dateFormatter.print(cityDateTime));
				cityProperties.add(timeFormatter.print(cityDateTime));
				cityProperties.add(updateProgress(city, progressBar));

				outputGrid.add(new ArrayList<String>(cityProperties));
				progressBar = updateProgress(city, progressBar);
			}
			System.out.println();
			System.out.println();

			outputGrid = Boolean.parseBoolean(properties.getProperty("pretty")) ? prettyFormat(outputGrid) : outputGrid;

			if (Boolean.parseBoolean(properties.getProperty("sort"))) {
				Collections.sort(unfound);
				Collections.sort(outputGrid, new Comparator<List<String>>() {
					public int compare(List<String> a, List<String> b) {
						return (a.get(LOCATION)).compareTo(b.get(LOCATION));
					}
				});
			}

			if (Boolean.parseBoolean(properties.getProperty("unfound"))) {
				System.out.println("These cities were not found:");

				for (String city : unfound) {
					System.out.println("\t- " + city);
				}
				System.out.println();
			}

			System.out.println("City Times:");
			for (List<String> city : outputGrid) {
				System.out.print("\t| ");
				if(Boolean.parseBoolean(properties.getProperty("location"))) System.out.print(city.get(LOCATION) + " | ");
				if(Boolean.parseBoolean(properties.getProperty("timezone"))) System.out.print(city.get(TIMEZONE) + " | ");
				if(Boolean.parseBoolean(properties.getProperty("date"))) System.out.print(city.get(DATE) + " | ");
				if(Boolean.parseBoolean(properties.getProperty("time"))) System.out.print(city.get(TIME) + " | ");
				System.out.println();
			}

			System.out.println();
		} while (requestRepeatProgram(userInput));
		userInput.close();
	}
	
	public static Properties applicationProperties() {
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream("./properties.txt"));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		return properties;
	}

	static private List<String> requestCities(Scanner userInput) {
		String[] cities;

		System.out.println(
				"\nEnter the cities that you would like the time of, " +
				"separate each city by a '|'.\n" +
				"If the incorrect city is displayed, try the following formats:\n" +
				"CITY, STATE/PROVINCE\n" +
				"CITY, STATE/PROVINCE, COUNTRY"
		);
		cities = userInput.nextLine().split("\\|");

		List<String> cleaned_cities = new ArrayList<String>();
		for (String city : cities) {
			cleaned_cities.add(city.trim());
		}

		return cleaned_cities;
	}

	static private Document retreiveCityDoc(String city) {
		if (city.isEmpty()) {
			return null;
		}

		try {
			return Jsoup.connect("http://www.time.is/" + city).get();
		} catch (Exception e) {
			return null;
		}
	}

	static private String getFullIdentity(Document cityDoc) {
		return cityDoc.select("#msgdiv").select("h1").text().replace("Time in ", "").replace(" now", "");
	}

	static private String getTimeZone(Document cityDoc) {
		String timeZoneID = cityDoc.select(".infobox").text();
		Pattern timeZonePattern = Pattern.compile("[\\w]+/[\\w]+(?=\\.)");
		Matcher timeZoneMatch = timeZonePattern.matcher(timeZoneID);
		
		timeZoneMatch.find();
		
		return timeZoneMatch.group(0);
	}

	static private List<List<String>> prettyFormat (List<List<String>> outputGrid) {
		int[] maxLength = new int[ATTRIBUTES];
		int[] padding = new int[ATTRIBUTES];

		for (List<String> city : outputGrid) {
			maxLength[LOCATION] = city.get(LOCATION).length() > maxLength[LOCATION] ? city.get(LOCATION).length() : maxLength[LOCATION];
			maxLength[TIMEZONE] = city.get(TIMEZONE).length() > maxLength[TIMEZONE] ? city.get(TIMEZONE).length() : maxLength[TIMEZONE];
			maxLength[DATE] = city.get(DATE).length() > maxLength[DATE] ? city.get(DATE).length() : maxLength[DATE];
			maxLength[TIME] = city.get(TIME).length() > maxLength[TIME] ? city.get(TIME).length() : maxLength[TIME];
		}

		int idx = 0;
		for (int pad : maxLength) {
			padding[idx++] = 0 - pad;
		}

		List<String> paddedList = new ArrayList<String>();
		for (idx = 0 ; idx < outputGrid.size() ; idx++) {
			paddedList.add(String.format("%" + padding[LOCATION] + "s", outputGrid.get(idx).get(LOCATION)));
			paddedList.add(String.format("%" + padding[TIMEZONE] + "s", outputGrid.get(idx).get(TIMEZONE)));
			paddedList.add(String.format("%" + padding[DATE] + "s", outputGrid.get(idx).get(DATE)));
			paddedList.add(String.format("%" + padding[TIME] + "s", outputGrid.get(idx).get(TIME)));

			outputGrid.set(idx, new ArrayList<String>(paddedList));
			paddedList.clear();
		}

		return outputGrid;
	}

	static private DateTimeFormatter buildDateFormat(Properties properties) {
		String dateFormat = "";

		if (Boolean.parseBoolean(properties.getProperty("weekday"))) {
			dateFormat += Boolean.parseBoolean(properties.getProperty("full_weekday")) ? "EEEE" : "EE";
			dateFormat += ", ";
		}
		dateFormat += Boolean.parseBoolean(properties.getProperty("full_month")) ? "MMMM " : "MM/";
		dateFormat += Boolean.parseBoolean(properties.getProperty("full_month")) ? "dd, " : "dd/";
		dateFormat += Boolean.parseBoolean(properties.getProperty("full_year")) ? "yyyy" : "yy";

		return DateTimeFormat.forPattern(dateFormat);
	}

	static private DateTimeFormatter buildTimeFormat(Properties properties) {
		String timeFormat = "";

		timeFormat += Boolean.parseBoolean(properties.getProperty("24hour")) ? "kk:mm" : "hh:mm";
		timeFormat += Boolean.parseBoolean(properties.getProperty("seconds")) ? ":ss" : "";
		timeFormat += Boolean.parseBoolean(properties.getProperty("24hour")) ? "" : " a";

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
				String.format("%.2f", current/total*100) + "%\r"
		);

		return progressBar;
	}

}
