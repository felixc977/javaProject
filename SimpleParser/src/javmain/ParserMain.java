package javmain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javparser.AvEntry;
import javparser.Parser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.nodes.Document;

public class ParserMain {
	static int idxStart = 1;
	static int idxRange = 500;
	static int idxAction = 1;
	static int idxEnd = 1;
	static int idxDigit = 3;
	static String strJsonFile = "123.json";
	static String strLabel = "IPZ";
	static String strQueryUrl = "http://www.javlibrary.com/tw/vl_searchbyid.php?keyword=";
	static String strGetIdUrl = "http://www.javlibrary.com/tw/?v=";
	static JSONArray jTotalData = new JSONArray();
	private final static Lock _mutex = new ReentrantLock(true);
	static boolean bFinish = false;

	public static void main(String[] args) throws Exception {
		try {
			if (args[0] != null) {
				System.out.printf("inputLabel:%s\n", args[0]);
				strLabel = args[0];
			}
			if (args[1] != null) {
				System.out.printf("digit:%d\n", Integer.valueOf(args[1]));
				idxDigit = Integer.valueOf(args[1]);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.printf("Null input\n");
		}

		/* Preload Json file */
		try {
			FileReader fileReader = new FileReader(strJsonFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String jsonText = bufferedReader.readLine();
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonText);
			jTotalData = (JSONArray) obj;
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			;
		}

		/* Main Part */
		String sLabelList = strLabel;
		String dirName = "./" + sLabelList;
		long total_startTime = System.currentTimeMillis();
		File fileJav = new File(dirName);
		if (fileJav.exists()) {
			System.out.println("dir exist\n");
		} else {
			System.out.println("dir create\n");
			System.out.println(fileJav.mkdir());
		}

		HelloThread t1 = new HelloThread("Fuck1");
		HelloThread t2 = new HelloThread("Fuck2");
		HelloThread t3 = new HelloThread("Fuck3");

		while (t1.t.isAlive() || t2.t.isAlive() || t3.t.isAlive())
		// while (t1.t.isAlive())
		{
			Thread.sleep(100);
			;
		}

		System.out.printf("----idxEnd =%d, Total Time=%d ms\n", idxEnd,
				System.currentTimeMillis() - total_startTime);

		StringWriter out = new StringWriter();
		jTotalData.writeJSONString(out);
		String jsonText2 = out.toString().replaceAll("\\\\", "");
		// System.out.print(jsonText);

		FileWriter fileWriter = new FileWriter(strJsonFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(jsonText2);
		bufferedWriter.close();
	}

	public static class HelloThread implements Runnable {
		String Name;
		Thread t;

		HelloThread(String sName) {
			this.Name = sName;
			t = new Thread(this, sName);
			t.start();
		}

		public void run() {
			while (true) {
				_mutex.lock();
				String sJavId = getNextItem();
				_mutex.unlock();
				if ((sJavId == null) || (bFinish == true)) {
					break;
				}
				System.out.printf("%s gogo\n", sJavId);
				doAction(sJavId);
			}
			System.out.printf("%s close\n", this.Name);
			return;
		}
	}

	@SuppressWarnings("unchecked")
	static void doAction(String javId) {
		boolean bHit = false;

		for (int i = 1; i < jTotalData.size(); i++) {
			JSONObject jTmpObj = (JSONObject) jTotalData.get(i);
			// System.out.printf("%s %s \n", jTmpObj.get("id"), javId);
			if (javId.matches((String) jTmpObj.get("id"))) {
				bHit = true;
				break;
			}
		}

		if (bHit == true) {
			System.out.printf("%s skip\n", javId);
			return;
		}

		long part_startTime = System.currentTimeMillis();
		System.out.printf("%s processing\n", javId);

		Document doc = null;
		try {
			doc = Parser.getUrl(javId);
			if (doc == null) {
				System.out.printf("%s not exist\n", javId);
				bFinish = true;
				return;
			}
		} catch (SocketTimeoutException e) {
			System.out.printf("%s timeout\n", javId);
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AvEntry avEntry = null;
		try {
			avEntry = Parser.getAttr(doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jObj = Parser.transData(avEntry);
		String dirName = "./" + strLabel;
		jTotalData.add(jObj);
		try {
			Parser.saveImage(avEntry.imgSrc, dirName + "/" + avEntry.id
					+ ".jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.printf("%s done\n", javId);
		System.out.printf("--Time=%d ms\n", System.currentTimeMillis()
				- part_startTime);
		// idxEnd = idx;
	}

	private static String getNextItem() {
		String javId = String.format("%s-%03d", strLabel, idxAction);
		if (idxAction == (idxStart + idxRange)) {
			return null;
		}
		idxAction = idxAction + 1;
		return javId;
	}
}
