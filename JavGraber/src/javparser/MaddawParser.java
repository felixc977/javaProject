package javparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jfm.ProcessListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MaddawParser {
	public static String strGetIdUrl = "http://maddawgjav.net/page/";
	public static String DbRoot = "./_DB_Madd/";
    public static String DbPath = DbRoot+"_DB_Madd.json";

	private static Vector<JavEntry> jDataList;
	private static JSONArray jTotalData;
	private static Vector<JavEntry> QueueA;
	private final static Lock _mutex = new ReentrantLock(true);
	private static PaserThread tb1 = new PaserThread("tb1");
	private static PaserThread tb2 = new PaserThread("tb2");
	private static PaserThread tb3 = new PaserThread("tb3");
	private static ProcessListener pListener = null;

	public MaddawParser() {
		try {
			init();
		} catch (IOException |ParseException e) {
			e.printStackTrace();
		}
	}

	public static void setListener(ProcessListener inListener) {
		pListener = inListener;
	}
	
	public static void init() throws IOException, ParseException {
		jTotalData = new JSONArray();
		jDataList = new Vector<JavEntry>();
		QueueA = new Vector<JavEntry>();
		
		File fileJav = new File(DbRoot);
		if (fileJav.exists()) {
			System.out.println("dir exist\n");
		} else {
			System.out.println("dir create\n");
			System.out.println(fileJav.mkdir());
		}
		
		try {
			FileReader fileReader = new FileReader(DbPath);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String jsonText = bufferedReader.readLine();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonText);
			jTotalData = (JSONArray) obj;
			
			for (int i = 0; i < jTotalData.size(); i++) {
				JavEntry jEntry = transJsonData((JSONObject) jTotalData.get(i));
				jDataList.add(jEntry);
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			;			
		}
	}
	
	public static void wakeupThreadB() {
		System.out.println("wakeupThreadB");
		if (!tb1.t.isAlive()) {
			tb1.start();
		}
		if (!tb2.t.isAlive()) {
			tb2.start();
		}
		if (!tb3.t.isAlive()) {
			tb3.start();
		}
	}
	
	public static void killThreadB() {
		System.out.println("killThreadB");
		if (tb1.t.isAlive()) {
			tb1.t.interrupt();
		}
		if (tb2.t.isAlive()) {
			tb2.t.interrupt();
		}
		if (tb3.t.isAlive()) {
			tb3.t.interrupt();
		}
	}
	
	public static boolean checkIfExist(String inId) {
		boolean bHit=false;
		for (int i = 0; i < jDataList.size(); i++) {
			JavEntry jTmpObj = jDataList.get(i);
			// System.out.printf("%s %s \n", jTmpObj.get("id"), javId);
			if (inId.matches(jTmpObj.id)) {
				bHit = true;
				break;
			}
		}
		return bHit;
	}
	
	public static void doAction(int actDepth) throws IOException {
		for (int i=1;i<=actDepth;i++) {
			String inUrl = String.format("%s%d", strGetIdUrl, i);
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
						
						if (checkIfExist(tmpV.id)) {
							System.out.println(" alreay exist!");
							continue;
						}
						
						tmpV.title=(link.html());
						tmpV.link =(link.attr("href").toString());
						
						QueueA.add(tmpV);
						wakeupThreadB();
					}
				}
			}
		}
		
		while (true) {
			if (QueueA.size()==0) {
				break;
			}else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static JavEntry getNextJob () {
		if (QueueA.size()>0) {
			if (pListener!=null){
				pListener.onEvent(QueueA.size()-1, 0);
			}
			return QueueA.remove(0);
		}
		return null;
	}
	
	public static void setNextJob (JavEntry inEntry) {
		QueueA.add(inEntry);
		if (pListener!=null){
			pListener.onEvent(QueueA.size(), 0);
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean parseSubPage() {
		_mutex.lock();
		JavEntry tmpV = getNextJob();
		_mutex.unlock();

		if (tmpV==null) {
			return false;
		}
		try {
			Document subDoc = Jsoup.connect(tmpV.link).userAgent("Mozilla").get();
			tmpV.imgSrc = getAttrImage(subDoc);
			tmpV.dllink = getAttrDLink(subDoc, tmpV.id);
			tmpV.cast = getAttrCast(subDoc);
			tmpV.date = getAttrDate(subDoc);
			tmpV.imgPath = DbRoot+tmpV.id+".jpg";
			saveImage(tmpV.imgSrc, tmpV.imgPath);
		} catch (IOException e1) {
			System.out.println("[Error] saveImage");
			tmpV.imgPath = "";
		}
		// Final
		_mutex.lock();
		jDataList.add(tmpV);
		JSONObject jObj = transData(jDataList.lastElement());
		jTotalData.add(jObj);
		_mutex.unlock();
		return true;
	}
	
	public static void close() throws IOException {
		/*Finish*/
		StringWriter out = new StringWriter();
		jTotalData.writeJSONString(out);
		String jsonText2 = out.toString().replaceAll("\\\\", "");

		FileWriter fileWriter = new FileWriter(DbPath);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(jsonText2);
		bufferedWriter.close();
		
		killThreadB();
	}

	@SuppressWarnings("unchecked")
	public static JSONObject transData(JavEntry avEntry) {
		JSONObject jObj = new JSONObject();
		jObj.put("title", avEntry.title);
		jObj.put("link", avEntry.link);
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
		
		JSONArray jArray3 = new JSONArray();
		for (String dllink : avEntry.dllink) {
			jArray3.add(dllink);
		}
		jObj.put("dllink", jArray3);

		return jObj;
	}
	
	public static JavEntry transJsonData(JSONObject jObj) {
		
		if (jObj==null){
			System.out.println("Null jObj");
			return null;
		}
		
		JavEntry avObj = new JavEntry();
		avObj.title = (String) jObj.get("title");
		avObj.link = (String) jObj.get("link");
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

		ArrayList<String> tmpArrayCast = new ArrayList<String>();
		JSONArray jArrayCast = (JSONArray)jObj.get("cast");
		for (Object castName :jArrayCast) {
			tmpArrayCast.add((String)castName);
		}
		avObj.cast = new Vector<String>(tmpArrayCast);
		
		ArrayList<String> tmpArrayGenre = new ArrayList<String>();
		JSONArray jArrayGenre = (JSONArray)jObj.get("genre");
		for (Object genreName :jArrayGenre) {
			tmpArrayGenre.add((String)genreName);
		}
		avObj.genre = new Vector<String>(tmpArrayGenre);
		
		ArrayList<String> tmpArrayDllink = new ArrayList<String>();
		JSONArray jArrayDllink = (JSONArray)jObj.get("dllink");
		for (Object dllink :jArrayDllink) {
			tmpArrayDllink.add((String)dllink);
		}
		avObj.dllink = new Vector<String>(tmpArrayDllink);

		return avObj;
	}
	
	public static String getAttrImage(Document doc) throws IOException {
		String imgSrc = null;
		Elements contents = doc.getElementsByClass("alignnone");
		imgSrc = contents.get(0).attr("src").toString();
		//System.out.println("imgsrc="+imgSrc);
		return imgSrc;
	}
	
	public static Vector<String> getAttrDLink(Document doc, String EntryId) throws IOException {
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
	
	public static Vector<String> getAttrCast(Document doc) throws IOException {
		Vector<String> vCast = new Vector<String>();
		Elements contents = doc.getElementsByTag("meta");
		for (Element content : contents) {
			if (content.attr("name").contains("keywords")) {
				String innerContent = content.attr("content").toString();
				String[] innerContentList = innerContent.split("[,\\s]+");
				for (int i=0;i<innerContentList.length;i++) {
					boolean bHit = false;
					//System.out.println(i+":"+innerContentList[i]);
					for (int j=0;j<vCast.size();j++) {
						if (vCast.get(j).contains(innerContentList[i])) {
							bHit = true;
							break;
						}
					}
					if (bHit==false) {
						vCast.add(innerContentList[i]);
					}
				}				
				break;
			}			
		}
		return vCast;
	}
	
	public static String getAttrDate(Document doc) throws IOException {
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

		String sRet = "";
		sRet = String.format("%04d%02d%02d", year, month, day);
		//System.out.println("getDate="+sRet);

		return sRet;
	}
	
	public static int length() {
		return jDataList.size();
	}
	
	public static JavEntry get(int idx) {
		return jDataList.get(idx);
	}

	public static void saveImage(String imageUrl, String destinationFile) throws IOException {
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