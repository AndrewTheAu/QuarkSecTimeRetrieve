import java.util.Scanner;
import java.util.regex.*;
import java.util.Calendar;
import java.util.TimeZone;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Test {

	public static void main(String args[]) {
		do {
			String city = requestCity(); // Will eventually be a string of cities
			getTimeZone(city);
			// FUNCTION PRINT THE TIME OF THE CITY FROM WEB
		} while (requestRepeatProgram());
	}

	static private String requestCity () {
		Scanner citiesUserInput = new Scanner(System.in);
		String cityTimes;

		System.out.println("Enter the cities that you would like the time of (separated by ','): ");
		cityTimes = citiesUserInput.nextLine();

		return cityTimes;
	}

	static private String getTimeZone(String city) {

		try {
			Document doc = Jsoup.connect("http://www.time.is/" + city).get();
			String timeZoneDescriptor = doc.select("#time_zone").select("ul").select("li:nth-child(2)").first().text();
			Pattern timeZonePattern = Pattern.compile("\\(.{3}\\)");
			Matcher timeZoneMatch = timeZonePattern.matcher(timeZoneDescriptor);
			
			timeZoneMatch.find();
			return timeZoneMatch.group(0).replaceAll("[()]", "");

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.out.println("Not Found");

			return "Not Found"
		}
	}

	static private boolean requestRepeatProgram() {
		Scanner userRepeatInput = new Scanner(System.in);

		System.out.print("Would you like to enter more cities (Y/n): ");
		while (true) {
			String repeatFlag = userRepeatInput.nextLine();
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
