import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Test {
	public static void main(String args[]) {
		Document doc;

		try {
			doc = Jsoup.connect("http://www.javatpoint.com").get();  
			String title = doc.title();
			System.out.println(title);
		} catch (Exception e) {
			System.err.println("Bad URL");
		}
	}
}
