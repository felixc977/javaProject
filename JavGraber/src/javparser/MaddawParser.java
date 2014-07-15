package javparser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

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
	private boolean bFinish = false;

	public MaddawParser() {
		jTotalData = new JSONArray();
		dataLst = new Vector<JavEntry>();
	}

	public JSONArray getJsonData() {
		return jTotalData;
	}

	public boolean isFinished() {
		return bFinish;
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
		
//		System.out.printf("transData imgPath  %s \n", avEntry.imgPath);

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
		
//		System.out.printf("transJsonData imgPath  %s \n", avObj.imgPath);

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
		boolean bRet = false;
		
		{
			Element content = doc.getElementById("content");
			if (content != null) {
				bRet = true;
			}
		}
		
		if (bRet) {
			Elements contents = doc.getElementsByTag("div");
			for (Element content : contents) {
				if (content.attr("id").contains("post-")) {
					JavEntry tmpV = new JavEntry();
					System.out.println("Hits! "+content.attr("id").toString());
					Elements links = content.getElementsByTag("a");
					Element link = links.get(0);
					System.out.println(link.attr("title").toString());
					System.out.println(link.attr("href").toString());
					
					tmpV.id=(content.attr("id").toString());
					tmpV.title=(link.attr("title").toString());
					tmpV.link =(link.attr("href").toString());
					dataLst.add(tmpV);
				}
			}
		}
	}
	
	public void doActionPart2() throws IOException {
		
		for(JavEntry tmpEntry:dataLst) {
			String inUrl = tmpEntry.link;
			Document doc = Jsoup.connect(inUrl).userAgent("Mozilla").get();
			Elements contents = doc.getElementsByClass("alignnone");
			for (Element content : contents) {
				System.out.println("imgsrc="+content.attr("src").toString());
				tmpEntry.imgSrc = content.attr("src").toString();
			}
			
			Element contentMain = doc.getElementById(tmpEntry.id);
			contents = contentMain.getElementsByTag("a");
			for (Element content : contents) {
				if (content.attr("href").contains("rapidgator")) {
					System.out.println("dllink="+content.attr("href").toString());
					tmpEntry.dllink.add(content.attr("href").toString());
				}				
			}
		}
	}

	public Document getUrl(String javId) throws IOException {
		String url = strQueryUrl + javId;
		Document doc = Jsoup.connect(url).userAgent("Mozilla").get();

		boolean bRet = false;

		{
			Element content = doc.getElementById("video_id");
			if (content != null) {
				bRet = true;
			}
		}

		if (!bRet) {
			Elements contents = doc.getElementsByClass("video");
			for (Element content : contents) {
				Elements links = content.getElementsByTag("a");
				for (Element link : links) {
					if (link.attr("title").contains(javId)) {
						String newKeyword = link.attr("href").substring(5);
						doc = Jsoup.connect(strGetIdUrl + newKeyword)
								.userAgent("Mozilla").get();
						bRet = true;
					}
				}
			}
		}

		if (bRet)
			return doc;
		else
			return null;
	}

	public static JavEntry getAttr(Document doc) throws IOException {
		JavEntry avEntry = new JavEntry();

		// 1.title
		{
			Element content = doc.getElementById("video_title");
			Elements links = content.getElementsByTag("a");
			for (Element link : links) {
				avEntry.title = link.text();
				// System.out.printf("title:%s\n", avEntry.title);
			}
		}

		// 2.image
		Element content2 = doc.getElementById("video_jacket_img");
		avEntry.imgSrc = content2.attr("src");
		// System.out.printf("img:%s\n", avEntry.imgSrc);

		// 3.id
		Element content3 = doc.getElementById("video_id");
		Elements links3 = content3.getElementsByClass("text");
		for (Element link : links3) {
			avEntry.id = link.text();
			// System.out.printf("id:%s\n", avEntry.id);
		}

		// 4.date
		{
			Element content = doc.getElementById("video_date");
			Elements links = content.getElementsByClass("text");
			for (Element link : links) {
				avEntry.date = link.text();
				// System.out.printf("date:%s\n", link.text());
			}
		}

		// length
		{
			Element content = doc.getElementById("video_length");
			Elements links = content.getElementsByClass("text");
			for (Element link : links) {
				avEntry.length = Integer.valueOf(link.text());
				// System.out.printf("length:%s\n", link.text());
			}
		}
		// director
		{
			Element content = doc.getElementById("video_director");
			Elements links = content.getElementsByClass("text");
			for (Element link : links) {
				avEntry.director = link.text();
				// System.out.printf("director:%s\n", link.text());
			}
		}
		// maker
		{
			Element content = doc.getElementById("video_maker");
			Elements links = content.getElementsByClass("text");
			for (Element link : links) {
				avEntry.maker = link.text();
				// System.out.printf("maker:%s\n", link.text());
			}
		}
		// label
		{
			Element content = doc.getElementById("video_label");
			Elements links = content.getElementsByClass("text");
			for (Element link : links) {
				avEntry.label = link.text();
				// System.out.printf("label:%s\n", link.text());
			}
		}
		// genre
		{
			Elements contents = doc.getElementsByClass("genre");
			// System.out.printf("genre:");
			for (Element content : contents) {
				Elements links = content.getElementsByTag("a");
				for (Element link : links) {
					avEntry.genre.add(link.text());
					// System.out.printf("%s ",
					// avEntry.genre.get(avEntry.genre.size()-1));
				}
			}
			// System.out.printf("\n");
		}
		// cast
		{
			Elements contents = doc.getElementsByClass("star");
			// System.out.printf("cast:");
			for (Element content : contents) {
				Elements links = content.getElementsByTag("a");
				for (Element link : links) {
					avEntry.cast.add(link.text());
					// System.out.printf("%s ",
					// avEntry.cast.get(avEntry.cast.size()-1));
				}
			}
			// System.out.printf("\n");
		}

		return avEntry;
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