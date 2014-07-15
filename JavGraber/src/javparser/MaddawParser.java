package javparser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaddawParser {
	String strQueryUrl = "http://www.javlibrary.com/tw/vl_searchbyid.php?keyword=";
    String strGetIdUrl = "http://maddawgjav.net/page/";

	private Vector<JavEntry> dataLst;
	private JSONArray jTotalData;

	public MaddawParser() {
		jTotalData = new JSONArray();
		dataLst = new Vector<JavEntry>();
	}

	public JSONArray getJsonData() {
		return jTotalData;
	}

	@SuppressWarnings("unchecked")
	public JSONObject transData(JavEntry avEntry) {
		JSONObject jObj = new JSONObject();
		jObj.put("title", avEntry.title);
		jObj.put("id", avEntry.id);
		jObj.put("date", avEntry.date);
		jObj.put("director", avEntry.director);
		jObj.put("maker", avEntry.maker);
		jObj.put("label", avEntry.label);
		jObj.put("imgSrc", avEntry.imgSrc);
		jObj.put("imgPath", avEntry.imgPath);
		jObj.put("length", avEntry.length);

		JSONArray jArray1 = new JSONArray();
		for (String castName : avEntry.cast) {
			jArray1.add(castName);
		}
		jObj.put("cast", jArray1);

		JSONArray jArray2 = new JSONArray();
		for (String genreType : avEntry.genre) {
			jArray2.add(genreType);
		}
		jObj.put("genre", jArray2);

		return jObj;
	}
	
	public JavEntry transJsonData(JSONObject jObj) {
		
		if (jObj==null){
			return null;
		}
		
		JavEntry avObj = new JavEntry();
		avObj.title = (String) jObj.get("title");
		avObj.imgSrc = (String) jObj.get("imgSrc");
		avObj.link = (String) jObj.get("link");
		avObj.imgPath = (String) jObj.get("imgPath");
		avObj.id = (String) jObj.get("id");
		avObj.date = (String) jObj.get("date");
		avObj.director = (String) jObj.get("director");
		avObj.label = (String) jObj.get("label");
		avObj.maker = (String) jObj.get("maker");
		try {
			avObj.length = (Integer) jObj.get("length");
		} catch (ClassCastException e) {
			;
		}

		ArrayList<String> tmpArrayCast = new ArrayList<String>();
		JSONArray jArrayCast = (JSONArray)jObj.get("cast");
		for (Object castName :jArrayCast) {
			tmpArrayCast.add((String)castName);
		}
		avObj.cast = new ArrayList<String>(tmpArrayCast);
		
		ArrayList<String> tmpArrayGenre = new ArrayList<String>();
		JSONArray jArrayGenre = (JSONArray)jObj.get("genre");
		for (Object genreName :jArrayGenre) {
			tmpArrayGenre.add((String)genreName);
		}
		avObj.genre = new ArrayList<String>(tmpArrayGenre);

		return avObj;
	}
	
	public void doAction() throws IOException {
		String inUrl = "http://maddawgjav.net/";
		Document doc = Jsoup.connect(inUrl).userAgent("Mozilla").get();
		
		Element content = doc.getElementById("content");
		if (content != null) {
			Elements divContents = doc.getElementsByTag("div");
			for (Element divContent : divContents) {
				if (divContent.attr("id").contains("post-")) {
					JavEntry tmpV = new JavEntry();
					Elements links = divContent.getElementsByTag("a");
					Element link = links.get(0);
					
					tmpV.id=(divContent.attr("id").toString());
					System.out.println("id: "+tmpV.id);
					tmpV.title=(link.html());
					System.out.println("title: "+tmpV.title);
					tmpV.link =(link.attr("href").toString());
					//System.out.println(tmpV.link);
				
					Document subDoc = Jsoup.connect(tmpV.link).userAgent("Mozilla").get();
					tmpV.imgSrc = getImage(subDoc);
					tmpV.dllink = getDLink(subDoc, tmpV.id);
					getDate(subDoc);
					// Final
					dataLst.add(tmpV);
				}
			}
		}
	}
	
	public String getImage(Document doc) throws IOException {
		String imgSrc = null;
		Elements contents = doc.getElementsByClass("alignnone");
		imgSrc = contents.get(0).attr("src").toString();
		//System.out.println("imgsrc="+imgSrc);
		return imgSrc;
	}
	
	public Vector<String> getDLink(Document doc, String EntryId) throws IOException {
		Vector<String> dlink = new Vector<String>();
		Element contentMain = doc.getElementById(EntryId);
		Elements contents = contentMain.getElementsByTag("a");
		for (Element content : contents) {
			if (content.attr("href").contains("rapidgator")) {
				//System.out.println("dllink="+content.attr("href").toString());
				dlink.add(content.attr("href").toString());
			}				
		}

		return dlink;
	}
	
	public int getDate(Document doc) throws IOException {
		Element content = doc.getElementsByClass("post-info-date").first();
		String dateInfo = content.html();
		int month = 1;
		int day = 1;
		int year = 2000;
		
		//Posted by noturbiatch on July 15, 2014
		String spatt = "([a-zA-Z]+)\\s([0-9]+),\\s([0-9]+)";
		Pattern patt = Pattern.compile(spatt);
		Matcher matcher = patt.matcher(dateInfo);
		if (matcher.find()) {
			for (int i=0;i<=matcher.groupCount();i++) {
				System.out.println(i+" = "+matcher.group(i));
			}
			year = Integer.parseInt(matcher.group(3));
			day = Integer.parseInt(matcher.group(2));
			switch (matcher.group(1)) {
			case "January":
				month = 1;
				break;
			case "February":
				month = 2;
				break;
			case "March":
				month = 3;
				break;
			case "April":
				month = 4;
				break;
			case "May":
				month = 5;
				break;
			case "June":
				month = 6;
				break;
			case "July":
				month = 7;
				break;
			case "August":
				month = 8;
				break;
			case "September":
				month = 9;
				break;
			case "October":
				month = 10;
				break;
			case "November":
				month = 11;
				break;
			case "December":
				month = 12;
				break;
			default:
				System.out.println("getDat: Default");
			}
		}

		System.out.println("getDate="+year+"/"+month+"/"+day);

		return 0;
	}

	public static void saveImage(String imageUrl, String destinationFile)
			throws IOException {
		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(destinationFile);

		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		is.close();
		os.close();
	}
}