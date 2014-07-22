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

import jfm.ProcessListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;

public abstract class JavParser {
	public String DbRoot = "./_DB_Default/";
    public String DbJsonPath = null;
    public String Name = "DefaultParser";

    protected Vector<JavEntry> jDataList;
    protected JSONArray jTotalData;
	protected Vector<JavEntry> QueueA;
	protected PaserThread tb1 = new PaserThread("tb1", this);
	protected PaserThread tb2 = new PaserThread("tb2", this);
	protected PaserThread tb3 = new PaserThread("tb3", this);
	protected ProcessListener pListener = null;

	/*Abstract Functions*/
	public abstract void parseAction(int actDepth);
	public abstract boolean parseSubPage();
	public abstract String getAttrImage(Document doc) throws IOException;
	public abstract Vector<String> getAttrDLink(Document doc, String EntryId) throws IOException;
	public abstract Vector<String> getAttrCast(Document doc) throws IOException;
	public abstract String getAttrDate(Document doc) throws IOException;
	
	public String getAttrRealId(String inStr) throws IOException {
		String realId = JavIdInducer.transId(inStr);
		//System.out.println("realID:"+realId);
		return realId;
	}
	
	public void setName(String inName) {
		Name = new String(inName);
	}
	
	public String getName() {
		return Name;
	}
	
	public void setListener(ProcessListener inListener) {
		pListener = inListener;
	}
	
	public void init(String inName) throws IOException, ParseException {
		setName(inName);
		DbRoot = String.format("./_DB_%s/", inName);
		DbJsonPath = String.format("%s_DB_%s.json", DbRoot,inName);
		
		jTotalData = new JSONArray();
		jDataList = new Vector<JavEntry>();
		QueueA = new Vector<JavEntry>();
		
		File fileJav = new File(DbRoot);
		if (!fileJav.exists()) {
			System.out.println("Create DB_Dir:"+fileJav.mkdir());
		}
		
		try {
			FileReader fileReader = new FileReader(DbJsonPath);
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
	
	public boolean ThreadBisWait() {
		if (tb1.isWait() && tb2.isWait() && tb3.isWait()) {
//		if (tb1.isWait() && tb2.isWait()) {
			return true;
		}
		return false;
	}

	public void wakeupThreadB() {
		//System.out.println("wakeupThreadB");
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
	
	public void killThreadB() {
		//System.out.println("killThreadB");
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
	
	public boolean checkIfExist(String inId) {
		boolean bHit=false;
		for (int i = 0; i < jDataList.size(); i++) {
			JavEntry jTmpObj = jDataList.get(i);
			if (inId.matches(jTmpObj.id)) {
				bHit = true;
				break;
			}
		}
		return bHit;
	}
	
	public void doAction(int actDepth) throws IOException {
		wakeupThreadB();
		parseAction(actDepth);
		
		while (true) {
			if ((QueueA.size()==0) && ThreadBisWait()) {
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
	
	public JavEntry getNextJob () {
		if (QueueA.size()>0) {
			if (pListener!=null){
				pListener.onEvent(QueueA.size()-1, 0);
			}
			return QueueA.remove(0);
		}
		return null;
	}
	
	public void setNextJob (JavEntry inEntry) {
		QueueA.add(inEntry);
		if (pListener!=null){
			pListener.onEvent(QueueA.size(), 0);
		}
	}
	
	public void close() throws IOException {
		/*Finish*/
		StringWriter out = new StringWriter();
		jTotalData.writeJSONString(out);
		String jsonText2 = out.toString().replaceAll("\\\\", "");

		FileWriter fileWriter = new FileWriter(DbJsonPath);
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

	public int length() {
		return jDataList.size();
	}
	
	public JavEntry get(int idx) {
		JavEntry retEntry = new JavEntry();
		if ((jDataList!=null)&&(length()!=0)) {
			retEntry =  jDataList.get(idx);
		}
		return retEntry;
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