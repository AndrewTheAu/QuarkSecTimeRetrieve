class Test {
	public static void main(String args[]) {
		Document doc = Jsoup.connect("http://www.javatpoint.com").get();  
		String title = doc.title();  
	}
}
